package io.github.oybek.gdetram

import java.util.concurrent.TimeUnit

import cats.syntax.all._
import cats.instances.option._
import cats.effect.{ExitCode, IO, IOApp, Resource, Sync}
import doobie.hikari.HikariTransactor
import io.github.oybek.gdetram.config.Config
import io.github.oybek.gdetram.db.DB
import io.github.oybek.gdetram.db.repository._
import io.github.oybek.gdetram.domain.model.Platform.{Tg, Vk}
import io.github.oybek.gdetram.domain.{Brain, BrainAlg}
import io.github.oybek.gdetram.service.{DocFetcherAlg, TabloidA}
import io.github.oybek.gdetram.service._
import io.github.oybek.gdetram.util.TimeTools._
import io.github.oybek.gdetram.util.vk.api.{VkApi, VkApiHttp4s}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.Logger
import org.slf4j.LoggerFactory
import telegramium.bots.client.{Api, ApiHttp4sImp}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object Main extends IOApp {
  type F[+T] = IO[T]

  private val log = LoggerFactory.getLogger("Main")

  override def run(args: List[String]): IO[ExitCode] =
    for {
      configFile <- Sync[F].delay {
        Option(System.getProperty("application.conf"))
      }
      config <- Config.load[F](configFile)
      _ <- Sync[F].delay { log.info(s"loaded config: $config") }
      _ <- resources(config)
        .use {
          case (transactor, httpClient) =>
            implicit val client           : Client[F]             = Logger(logHeaders = false, logBody = false)(httpClient)
            implicit val cityRepo         : CityRepoAlg[F]        = new CityRepo[F](transactor)
            implicit val journalRepo      : JournalRepoAlg[F]     = new JournalRepo(transactor)
            implicit val stopRepo         : StopRepoAlg[F]        = new StopRepo(transactor)
            implicit val userRepo         : UserRepoAlg[F]        = new UserRepo[F](transactor)
            implicit val documentFetcher  : DocFetcherAlg[F]      = new DocFetcher[F]
            implicit val messageRepo      : MessageRepoAlg[F]     = new MessageRepo[F](transactor)
            implicit val vkBotApi         : VkApi[F]              = new VkApiHttp4s[F](client)
            implicit val tgBotApi         : Api[F]                = new ApiHttp4sImp[F](client, s"https://api.telegram.org/bot${config.tgBotApiToken}")
            implicit val source1          : TabloidAlg[F]         = new TabloidA[F]
            implicit val core             : BrainAlg[F]           = new Brain[F]
            implicit val metricService    : MetricServiceAlg[F]   = new MetricService[F]()

            val vkBot = new VkBot[F](config.getLongPollServerReq)
            val tgBot = new TgBot[F](config.adminTgIds.split(",").map(_.trim).toList)

            for {
              _ <- DB.initialize(transactor)
              f1 <- vkBot.start.start
              f2 <- tgBot.start.start

              _ <- tgBot.dailyReports().everyDayAt( 9, 30).start
              _ <- tgBot.dailyMetricsDump.everyDayAt(23, 59).start
              _ <- messageRepo
                .pollSyncMessage
                .flatMap {
                  case Some((Vk, id, text)) => vkBot.sendMessage(id, text)
                  case Some((Tg, id, text)) => tgBot.sendMessage(id.toInt, text)
                  case _ => Sync[F].unit
                }.every(30.seconds, 9*60*2).everyDayAt(7, 0)

              _ <- f1.join
              _ <- f2.join
            } yield ()
        }
    } yield ExitCode.Success

  private def resources(
    config: Config
  ): Resource[F, (HikariTransactor[F], Client[F])] = {
    for {
      transactor <- DB.transactor[F](config.database)
      httpClient <- BlazeClientBuilder[F](global)
        .withResponseHeaderTimeout(FiniteDuration(60, TimeUnit.SECONDS))
        .resource
    } yield (transactor, httpClient)
  }
}
