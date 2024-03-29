package io.github.oybek.gdetram

import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import cats.instances.list._
import cats.instances.option._
import cats.syntax.all._
import cats.~>
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.{ConnectionIO, ExecutionContexts}
import io.github.oybek.gdetram.config.Config
import io.github.oybek.gdetram.dao._
import io.github.oybek.gdetram.dao.impl._
import io.github.oybek.gdetram.model.Platform.{Tg, Vk}
import io.github.oybek.gdetram.service._
import io.github.oybek.gdetram.service.impl._
import io.github.oybek.gdetram.util.TimeTools._
import io.github.oybek.gdetram.util.Timer
import io.github.oybek.vkontaktum.api.{GetConversationsReq, GetLongPollServerReq, Unanswered, VkApi, VkApiHttp4s}
import io.github.oybek.vkontaktum.domain.{Conversation, Peer}
import org.http4s.client.Client
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import org.slf4j.LoggerFactory
import telegramium.bots.high.{Api, BotApi}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{FiniteDuration, _}

object Main extends IOApp {
  type F[+T] = IO[T]
  type G[T] = ConnectionIO[T]

  private val log = LoggerFactory.getLogger("Main")
  implicit val timer: Timer[IO] = (finiteDuration: FiniteDuration) => IO.sleep(finiteDuration)

  override def run(args: List[String]): F[ExitCode] =
    Config.load match {
      case Right(config) =>
        log.info(s"loaded config: $config")
        for {
          _ <- resources(config).use {
            case (transactor, httpClient) =>
              assembleAndLaunch(
                config,
                transactor,
                httpClient
              )
          }
        } yield ExitCode.Success

      case Left(_) =>
        log.error("Could not load config file").pure[F].as(ExitCode.Error)
    }

  def assembleAndLaunch(config: Config,
                        transactor: HikariTransactor[F],
                        httpClient: Client[F]): F[ExitCode] = {
    implicit val transaction: ~>[G, F] = new ~>[G, F] {
      override def apply[A](cio: G[A]): F[A] =
        cio.transact(transactor)
    }

    implicit val client: Client[F] = Logger(logHeaders = false, logBody = false)(httpClient)

    implicit val cityRepo: CityRepo[G] = CityRepoImpl
    implicit val stopRepo: StopRepo[G] = StopRepoImpl
    implicit val userRepo: UserRepo[G] = UserRepoImpl
    implicit val messageRepo: MessageRepo[G] = MessageRepoImpl
    implicit val journalRepo: JournalRepo[G] = JournalRepoImpl

    implicit val tabloidService: TabloidService[F] = new TabloidServiceImpl[F]

    implicit val startService: StartService[F] = new StartService[F]
    implicit val registrationService: RegistrationService[F, G] = new RegistrationService[F, G]
    implicit val cityService: CityService[F, G] = new CityService[F, G]
    implicit val stopService: StopService[F, G] = new StopService[F, G]
    implicit val statusService: StatusService[F, G] = new StatusService[F, G]

    implicit val core: Logic[F] = new LogicImpl[F, G]
    implicit val metricService: MetricService[F, G] = new MetricServiceImpl[F, G]

    implicit val vkBotApi: VkApi[F] = new VkApiHttp4s[F](client)
    implicit val tgBotApi: Api[F] = BotApi[F](client, s"https://api.telegram.org/bot${config.tgBotApiToken}")

    implicit val vkBot: VkBot[F] = new VkBot[F](config.getLongPollServerReq)
    implicit val tgBot: TgBot[F, G] = new TgBot[F, G](config.adminTgIds.split(",").map(_.trim).toList)

    for {
      _ <- DB.initialize(transactor)
      _ <- vkBot.start.start
      _ <- tgBot.start.start
      _ <- tgBot.dailyReports("Ну что уебаны?! Готовы к метрикам?".some).everyDayAt(8, 0).start
      _ <- transaction(UserServiceImpl.refreshUserInfo).attempt.void.everyDayAt(0, 0).start
      _ <- vkRevoke(vkBotApi, vkBot, config.getLongPollServerReq).start
      _ <- spamTg(messageRepo)
    } yield ExitCode.Success
  }

  private def spamTg(messageRepo: MessageRepo[G])(implicit
                                                  tgBot: TgBot[F, G],
                                                  transaction: G ~> F): F[Unit] = (
    for {
      messages <- transaction(messageRepo.pollSyncMessage(Tg))
      _ <- Sync[F].delay(log.info(s"sync_message tg: $messages"))
      _ <- messages.toNel.traverse(_.head match {
        case (Tg, id, text) => tgBot.send(id.toInt, text)
        case _ => ().pure[F]
      })
    } yield ()
    ).attempt.void.every(10.seconds, (0, 23))

  private def resources(config: Config): Resource[F, (HikariTransactor[F], Client[F])] = {
    for {
      httpCp <- ExecutionContexts.cachedThreadPool[F]
      tranEc <- ExecutionContexts.cachedThreadPool[F]
      transactor <- DB.transactor[F](config.database, tranEc)
      httpClient <- BlazeClientBuilder[F]
        .withExecutionContext(httpCp)
        .withResponseHeaderTimeout(FiniteDuration(60, TimeUnit.SECONDS))
        .resource
    } yield (transactor, httpClient)
  }

  private def spamVk(messageRepo: MessageRepo[G])(implicit
                                                  vkBot: VkBot[F],
                                                  transaction: G ~> F): F[Unit] = (
    for {
      messages <- transaction(messageRepo.pollSyncMessage(Vk, 100))
      _ <- Sync[F].delay(log.info(s"sync_message vk: $messages"))
      _ <- messages.toNel.traverse(messages1 =>
        vkBot.sendMessage(
          Right(messages1.map(_._2).some),
          messages1.head._3,
        )
      )
    } yield ()
    ).every(10.seconds, (9, 20))

  private def vkRevoke(vkApi: VkApi[F],
                       vkBot: VkBot[F],
                       getLongPollServerReq: GetLongPollServerReq,
                       offset: Int = 0,
                       count: Int = 100): F[Unit] =
    for {
      getConversationsRes <- vkApi.getConversations(
        GetConversationsReq(
          filter = Unanswered,
          offset = offset,
          count = count,
          version = getLongPollServerReq.version,
          accessToken = getLongPollServerReq.accessToken
        )
      )
      _ <- getConversationsRes.response.items.map(_.conversation)
        .map { case Conversation(Peer(peerId, _, _), _) => peerId.toLong }
        .grouped(100).toList
        .flatMap(NonEmptyList.fromList)
        .traverse {
          peerIds =>
            vkBot.sendMessage(Right(Some(peerIds)), "Не забыли про меня? Напишите название остановки - я подскажу время прибытия транспорта").attempt >>
              Timer[F].sleep(30 seconds)
        }
    } yield ()
}
