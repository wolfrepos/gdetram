package io.github.oybek.gdetram.service.impl

import cats.Applicative
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import io.github.oybek.gdetram.model.Message
import io.github.oybek.gdetram.service.Replies.cityAsk
import io.github.oybek.gdetram.model.Message.Text
import io.github.oybek.gdetram.service.{Handler, Reply, UserId}

class StartService[F[_]: Applicative] extends Handler[F, Message, Unit] {

  override val handle: Message => F[Either[Reply, Unit]] = {
    case Text("начать" | "/start") =>
      cityAsk(withGreeting = true).asLeft[Unit].pure[F]

    case _ =>
      ().asRight[Reply].pure[F]
  }
}
