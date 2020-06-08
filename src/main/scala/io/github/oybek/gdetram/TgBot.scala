package io.github.oybek.gdetram

import java.sql.Timestamp

import cats.effect.syntax.all._
import cats.effect.{Async, Concurrent, Sync, Timer}
import cats.syntax.all._
import io.github.oybek.gdetram.db.repository.JournalRepoAlg
import io.github.oybek.gdetram.domain.BrainAlg
import io.github.oybek.gdetram.domain.model.Platform.Tg
import io.github.oybek.gdetram.service.MetricServiceAlg
import io.github.oybek.gdetram.util.TgExtractors
import io.github.oybek.gdetram.util.vk.Coord
import org.slf4j.{Logger, LoggerFactory}
import telegramium.bots.client.Api
import telegramium.bots.high.LongPollBot

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

  def dailyReports(chatId: ChatId = ChatIntId(-391934727)): F[Unit] =
    metricService.mainMetrics.flatMap(sendIt(chatId, _))

  def sendIt(chatId: ChatId, cc: (java.io.File, String)): F[Unit] = cc match {
    case (chart, caption) =>
      bot.sendPhoto(SendPhotoReq(chatId, InputPartFile(chart))) *>
        bot.sendMessage(SendMessageReq(chatId, caption)).void
  }

  def dailyMetricsDump: F[Unit] =
    journalRepo.dailyMetricsDump.void

  override def onCallbackQuery(query: CallbackQuery): F[Unit] =
    Sync[F].delay { log.info(s"got query: $query") } *> (query match {
      case CallbackQuery(_, _, Some(message), _, _, Some(text), _) =>
        for {
          reply <- core.handleText(Tg -> message.chat.id, text)
          _ <- sendMessage(message.chat.id, reply._1, Some(reply._2.toTg))
        } yield ()
      case _ => Sync[F].unit
    })

  override def onMessage(message: Message): F[Unit] =
    Sync[F].delay { log.info(s"got message: $message") } *> (message match {
      case Location(location) =>
        core
          .handleGeo(Tg -> message.chat.id, Coord(location.latitude, location.longitude))
          .flatMap(reply => sendMessage(message.chat.id, reply._1, Some(reply._2.toTg)))

      case Text("/stat") if adminIds.contains(message.chat.id.toString) =>
        metricService.mainMetrics.flatMap(sendIt(ChatIntId(message.chat.id), _))

      case Text("/stat platform") if adminIds.contains(message.chat.id.toString) =>
        metricService.platformPie.flatMap(sendIt(ChatIntId(message.chat.id), _))

      case Text("/stat city") if adminIds.contains(message.chat.id.toString) =>
        metricService.cityPie.flatMap(sendIt(ChatIntId(message.chat.id), _))

      case Text(text) =>
        core
          .handleText(Tg -> message.chat.id, text)
          .flatMap(reply => sendMessage(message.chat.id, reply._1, Some(reply._2.toTg)).start.void)

      case _ =>
        Sync[F].unit
    })

  private def sendMessage(chatId: Int,
                          text: String,
                          keyboardOpt: Option[KeyboardMarkup] = None): F[Unit] = {
    val sendMessageReq =
      SendMessageReq(
        chatId = ChatIntId(chatId),
        text = text,
        replyMarkup = keyboardOpt
      )
    bot.sendMessage(sendMessageReq).void *>
      Sync[F].delay { log.info(s"send message: $sendMessageReq") }
  }

}
