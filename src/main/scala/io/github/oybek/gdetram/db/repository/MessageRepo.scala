package io.github.oybek.gdetram.db.repository

import cats.effect.Sync
import doobie.util.transactor.Transactor
import doobie.implicits._
import io.github.oybek.gdetram.domain.model.{Platform, PsMessage}

trait MessageRepoAlg[F[_]] {
  def insertMessage(message: PsMessage): F[Int]
  def selectNotDeliveredMessageFor(
    user: (Platform, Long)
  ): F[Option[PsMessage]]

  def insertDelivered(messageId: Int, user: (Platform, Long)): F[Int]
}

class MessageRepo[F[_]: Sync](transactor: Transactor[F])
    extends MessageRepoAlg[F] {
  override def insertMessage(message: PsMessage): F[Int] =
    Queries.insertMessageSql(message).run.transact(transactor)

  override def selectNotDeliveredMessageFor(
    user: (Platform, Long)
  ): F[Option[PsMessage]] =
    Queries.getNotDeliveredMessageForSql(user).option.transact(transactor)

  override def insertDelivered(messageId: Int, user: (Platform, Long)): F[Int] =
    Queries.markDeliveredForUserSql(messageId, user).run.transact(transactor)
}
