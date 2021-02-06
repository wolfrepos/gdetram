package io.github.oybek.gdetram.domain.chain

import io.github.oybek.gdetram.domain.chain.model.Input
import io.github.oybek.gdetram.domain.model.{Button, GeoButton}

trait Handler[F[_], A, B] {
  def handle(data: B)(implicit input: Input): F[Either[Reply, A]]

  protected def defaultKbrd(topButton: Button*): List[List[Button]] =
    List(topButton.toList, List(GeoButton))
}
