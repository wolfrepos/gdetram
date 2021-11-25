package io.github.oybek.gdetram.service

import io.github.oybek.gdetram.model.{Message, User}

trait AuthorizedHandler[F[_], B] {
  def handle(user: User, message: Message): F[Either[Reply, B]]
}
