package io.github.oybek.gdetram.db.repository

import cats.effect.Sync
import doobie.util.transactor.Transactor
import io.github.oybek.gdetram.model.{Platform, SpamMessage}
import doobie.implicits._

trait MessageRepoAlg[F[_]] {
  def insertMessage(message: SpamMessage): F[Int]
  def selectNotDeliveredMessageFor(
    user: (Platform, Long)
  ): F[Option[SpamMessage]]

  def insertDelivered(messageId: Int, user: (Platform, Long)): F[Int]
}

class MessageRepo[F[_]: Sync](transactor: Transactor[F])
    extends MessageRepoAlg[F] {
  override def insertMessage(message: SpamMessage): F[Int] =
    Queries.insertMessageSql(message).run.transact(transactor)

  override def selectNotDeliveredMessageFor(
    user: (Platform, Long)
  ): F[Option[SpamMessage]] =
    Queries.getNotDeliveredMessageForSql(user).option.transact(transactor)

  override def insertDelivered(messageId: Int, user: (Platform, Long)): F[Int] =
    Queries.markDeliveredForUserSql(messageId, user).run.transact(transactor)
}
