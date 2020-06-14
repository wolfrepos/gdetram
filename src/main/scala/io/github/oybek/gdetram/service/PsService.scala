package io.github.oybek.gdetram.service

import cats.effect.Sync
import cats.instances.option._
import cats.syntax.all._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.oybek.gdetram.db.repository.Queries
import io.github.oybek.gdetram.domain.model.Platform

trait PsServiceAlg[F[_]] {
  // creates row in 'message' table
  def createMessage(text: String): F[Int]

  // gives text field of first message not in delivered
  def getNotDeliveredMessageFor(user: (Platform, Long)): F[Option[String]]
}

class PsService[F[_]: Sync](tx: Transactor[F])
  extends PsServiceAlg[F] {
  override def createMessage(text: String): F[Int] =
    Queries.insertMessageSql(text).run.transact(tx)

  override def getNotDeliveredMessageFor(user: (Platform, Long)): F[Option[String]] = (
    for {
      msgOpt <- Queries.getNotDeliveredMessageForSql(user).option
      _ <- msgOpt.traverse(msg =>
        Queries.markDeliveredForUserSql(msg.id, user).run
      )
    } yield msgOpt.map(_.text)
  ).transact(tx)
}
