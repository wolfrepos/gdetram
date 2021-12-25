package io.github.oybek.gdetram.service

trait Handler[F[_], A, B] {
  val handle: A => F[Either[Reply, B]]
}
