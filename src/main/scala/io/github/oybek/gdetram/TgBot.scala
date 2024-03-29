package io.github.oybek.gdetram

import cats.{Monad, Parallel}
import cats.effect.syntax.all._
import cats.effect.{Async, Concurrent, Sync}
import cats.instances.option._
import cats.syntax.all._
import io.github.oybek.gdetram.dao.JournalRepo
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.model.Message.{Geo, Text}
import io.github.oybek.gdetram.service.{Logic, MetricService}
import io.github.oybek.gdetram.util.{TgExtractors, Timer}
import org.slf4j.{Logger, LoggerFactory}
import telegramium.bots.high._
import telegramium.bots.high.implicits._

import scala.concurrent.duration.DurationInt

class TgBot[F[_]: Async: Timer: Parallel, G[_]: Monad](adminIds: List[String])
                                                      (implicit bot: Api[F],
                                                                core: Logic[F],
                                                                journalRepo: JournalRepo[G],
                                                                metricService: MetricService[F, G])
    extends LongPollBot[F](bot)
    with TgExtractors {

  val log: Logger = LoggerFactory.getLogger("TgGate")

  import telegramium.bots._

  def dailyReports(preMessage: Option[String] = None, chatId: Long = -391934727): F[Unit] =
    preMessage.traverse_(send(chatId, _) >> Timer[F].sleep(2.seconds)) >>
      metricService.userStats.flatMap(send(chatId, _))

  override def onCallbackQuery(query: CallbackQuery): F[Unit] = (
    Sync[F].delay { log.info(s"got query: $query") } >> (query match {
      case CallbackQuery(_, _, Some(message), _, _, Some(text), _) =>
        for {
          reply <- core.handle(Tg -> message.chat.id, Text(text))
          _ <- Methods.editMessageText(
            chatId = ChatIntId(message.chat.id).some,
            messageId = message.messageId.some,
            text = reply._1,
            replyMarkup = reply._2.toTg.some
          ).exec.void
          _ <- Sync[F].delay { log.info(s"update message: ${message.messageId} from ${message.chat.id}") }
        } yield ()
      case _ => Sync[F].unit
    })
  ).start.void

  override def onMessage(message: Message): F[Unit] = (
    Sync[F].delay { log.info(s"got message: $message") } >> (message match {
      case LocationMessage(location) =>
        core
          .handle(Tg -> message.chat.id, Geo(location.latitude, location.longitude))
          .flatMap(reply => send(message.chat.id, reply._1, Some(reply._2.toTg)))

      case TextMessage("/stats") =>
        dailyReports(chatId = message.chat.id)

      case TextMessage(text) =>
        core
          .handle(Tg -> message.chat.id, Text(text))
          .flatMap(reply => send(message.chat.id, reply._1, Some(reply._2.toTg)))

      case _ =>
        Sync[F].unit
    })
  ).start.void

  def send(chatId: Long,
           text: String,
           keyboardOpt: Option[KeyboardMarkup] = None): F[Unit] =
    Methods.sendMessage(
      chatId = ChatIntId(chatId),
      text = text,
      replyMarkup = keyboardOpt
    ).exec.void >>
      Sync[F].delay { log.info(s"send message: $text to $chatId") }

}
