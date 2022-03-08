package io.github.oybek.gdetram

import cats.effect.{Async, Resource, Sync}
import doobie.hikari.HikariTransactor
import io.github.oybek.gdetram.config.Config.DatabaseConfig
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object DB {
  def transactor[F[_]: Sync: Async](
    config: DatabaseConfig,
    ec: ExecutionContext,
  ): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      ec
    )

  def initialize[F[_]: Sync](transactor: HikariTransactor[F]): F[Unit] = transactor.configure(
    dataSource =>
      Sync[F].delay {
        Flyway
          .configure()
          .dataSource(dataSource)
          .load()
          .migrate()
        ()
      }
  )
}
