package io.github.oybek.gdetram

import cats.effect.concurrent.Ref
import cats.effect.syntax.all._
import cats.effect.{Async, Clock, Concurrent, Sync, Timer}
import cats.syntax.all._
import io.github.oybek.gdetram.db.repository.JournalRepoAlg
import io.github.oybek.gdetram.model.Platform.Vk
import io.github.oybek.gdetram.service.CoreAlg
import io.github.oybek.gdetram.util.Formatting._
import io.github.oybek.gdetram.util.vk.api._
import io.github.oybek.gdetram.util.vk._
import org.http4s.client.Client
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.MILLISECONDS

class VkBot[F[_]: Async: Timer: Concurrent](getLongPollServerReq: GetLongPollServerReq)(implicit httpClient: Client[F],
                                                                                        core: CoreAlg[F],
                                                                                        vkApi: VkApi[F])
    extends LongPollBot[F](httpClient, vkApi, getLongPollServerReq) {

  implicit val log: Logger = LoggerFactory.getLogger("VkGate")

  override def onMessageNew(message: MessageNew): F[Unit] =
    Sync[F].delay { log.info(s"got message $message") } *>
      (message match {
      // TODO: use custom extractors for pattern matching
      // https://stackoverflow.com/questions/39139815/pattern-matching-on-big-long-case-classes
      case MessageNew(_, _, peerId, _, _, Some(Geo(coord, _))) =>
        for {
          answer <- core.handleGeo(Vk -> peerId, coord)
          _ <- sendMessage(
            peerId,
            text = answer._1,
            keyboard = answer._2.toVk.some
          ).start.void
        } yield ()
      case MessageNew(_, _, peerId, _, text, _) =>
        for {
          answer <- core.handleText(Vk -> peerId, text)
          _ <- sendMessage(
            peerId,
            text = answer._1,
            keyboard = answer._2.toVk.some
          ).start.void
        } yield ()
    })

  override def onWallPostNew(wallPostNew: WallPostNew): F[Unit] = Sync[F].unit

  override def onWallReplyNew(wallReplyNew: WallReplyNew): F[Unit] =
    Sync[F].unit

  private def sendMessage(to: Long,
                          text: String,
                          attachment: Option[String] = None,
                          keyboard: Option[Keyboard] = None): F[Unit] = {
    val sendMessageReq = SendMessageReq(
      peerId = to,
      message = text,
      version = getLongPollServerReq.version,
      randomId = 0,
      accessToken = getLongPollServerReq.accessToken,
      attachment = attachment,
      keyboard = keyboard
    )
    for {
      time <- Clock[F].realTime(MILLISECONDS)
      _ <- vkApi.sendMessage(sendMessageReq.copy(randomId = time)).void
      _ <- Sync[F].delay { log.info(s"send message: $sendMessageReq") }
    } yield ()
  }
}
