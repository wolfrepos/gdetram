package io.github.oybek.gdetram.service

import cats.effect.Sync
import cats.instances.option._
import cats.syntax.all._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.oybek.gdetram.db.repository.Queries
import io.github.oybek.gdetram.domain.model.Platform

trait MessageRepoAlg[F[_]] {
  def pollAsyncMessage(user: (Platform, Long)): F[Option[String]]
  def pollSyncMessage: F[Option[(Platform, Long, String)]]
}

class MessageRepo[F[_]: Sync](tx: Transactor[F]) extends MessageRepoAlg[F] {
  override def pollAsyncMessage(user: (Platform, Long)): F[Option[String]] = (
    for {
      msgOpt <- Queries.getAsyncMessageFor(user).option
      _ <- msgOpt.traverse(text => Queries.delAsyncMessageFor(user, text).run)
    } yield msgOpt
  ).transact(tx)

  override def pollSyncMessage: F[Option[(Platform, Long, String)]] = (
    for {
      msgOpt <- Queries.getSyncMessage.option
      _ <- msgOpt.traverse { case (platform, id, text) =>
        Queries.delSyncMessageFor((platform, id), text).run
      }
    } yield msgOpt
  ).transact(tx)
}
