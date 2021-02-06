package io.github.oybek.gdetram.domain.chain

import cats.{Applicative, Monad}
import cats.implicits._
import io.github.oybek.gdetram.db.repository.MessageRepoAlg
import io.github.oybek.gdetram.domain.chain.model.Input

class PsHandler[F[_]: Applicative: Monad](implicit
                                          messageRepo: MessageRepoAlg[F]) extends Handler[F, Unit, (UserId, Reply)] {
  override def handle(data: (UserId, Reply))
                     (implicit input: Input): F[Either[Reply, Unit]] =
    data match {
      case (userId, (replyText, replyKbrd)) =>
        messageRepo.pollAsyncMessage(userId).map(psText =>
          (replyText + psText.fold("")("\n" + _), replyKbrd).asLeft[Unit]
        )
    }
}
