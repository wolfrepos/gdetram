package io.github.oybek.gdetram.service

import cats.effect.Sync
import cats.implicits._
import cats.effect.syntax.all._
import io.github.oybek.gdetram.db.repository.{MessageRepo, MessageRepoAlg}
import io.github.oybek.gdetram.model.{Platform, SpamMessage}

class SpamService[F[_]: Sync](implicit messageRepo: MessageRepoAlg[F])
    extends SpamServiceAlg[F] {
  override def createMessage(text: String): F[Int] =
    messageRepo.insertMessage(SpamMessage(text = text))

  override def getNotDeliveredMessageFor(
    user: (Platform, Long)
  ): F[Option[String]] =
    for {
      messageOpt <- messageRepo.selectNotDeliveredMessageFor(user)
      _ <- messageOpt.traverse { message =>
        messageRepo.insertDelivered(message.id, user)
      }
    } yield messageOpt.map(_.text)
}
