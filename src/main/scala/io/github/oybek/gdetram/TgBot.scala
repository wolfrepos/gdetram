package io.github.oybek.gdetram

import cats.effect.syntax.all._
import cats.effect.{Async, Concurrent, Sync, Timer}
import cats.syntax.all._
import cats.instances.option._
import io.github.oybek.gdetram.db.repository.JournalRepoAlg
import io.github.oybek.gdetram.domain.BrainAlg
import io.github.oybek.gdetram.domain.model.Platform.Tg
import io.github.oybek.gdetram.service.MetricServiceAlg
import io.github.oybek.gdetram.util.TgExtractors
import io.github.oybek.vk4s.domain.Coord
import org.slf4j.{Logger, LoggerFactory}
import telegramium.bots.high._
import telegramium.bots.high.implicits._

import scala.concurrent.duration.DurationInt

class TgBot[F[_]: Async: Timer: Concurrent](adminIds: List[String])
                                           (implicit bot: Api[F],
                                            core: BrainAlg[F],
                                            journalRepo: JournalRepoAlg[F],
                                            metricService: MetricServiceAlg[F])
    extends LongPollBot[F](bot)
    with TgExtractors {

  val log: Logger = LoggerFactory.getLogger("TgGate")

  import telegramium.bots._
  import telegramium.bots.client._

  def dailyReports(preMessage: Option[String] = None, chatId: Int = -391934727): F[Unit] =
    preMessage.traverse_(send(chatId, _) >> Timer[F].sleep(2.seconds)) >>
      metricService.userStats.flatMap(send(chatId, _))

  override def onCallbackQuery(query: CallbackQuery): F[Unit] = (
    Sync[F].delay { log.info(s"got query: $query") } >> (query match {
      case CallbackQuery(_, _, Some(message), _, _, Some(text), _) =>
        for {
          reply <- core.handleText(Tg -> message.chat.id, text)
          _ <- send(message.chat.id, reply._1, Some(reply._2.toTg))
        } yield ()
      case _ => Sync[F].unit
    })
  ).start.void

  override def onMessage(message: Message): F[Unit] = (
    Sync[F].delay { log.info(s"got message: $message") } >> (message match {
      case Location(location) =>
        core
          .handleGeo(Tg -> message.chat.id, Coord(location.latitude, location.longitude))
          .flatMap(reply => send(message.chat.id, reply._1, Some(reply._2.toTg)))

      case Text("/stat" | "/stat@gdetrambot") => dailyReports("Тебе хуемразь отдельно повторить?!".some)

      case Text(text) =>
        core
          .handleText(Tg -> message.chat.id, text)
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
