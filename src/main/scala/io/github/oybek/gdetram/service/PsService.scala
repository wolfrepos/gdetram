package io.github.oybek.gdetram.service

import cats.effect.Sync
import cats.syntax.all._
import cats.instances.option._
import io.github.oybek.gdetram.db.repository.MessageRepoAlg
import io.github.oybek.gdetram.domain.model.{Platform, PsMessage}

trait PsServiceAlg[F[_]] {
  // creates row in 'message' table
  def createMessage(text: String): F[Int]

  // gives text field of first message not in delivered
  def getNotDeliveredMessageFor(user: (Platform, Long)): F[Option[String]]
}

class PsService[F[_]: Sync](implicit messageRepo: MessageRepoAlg[F])
  extends PsServiceAlg[F] {
  override def createMessage(text: String): F[Int] =
    messageRepo.insertMessage(PsMessage(text = text))

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
