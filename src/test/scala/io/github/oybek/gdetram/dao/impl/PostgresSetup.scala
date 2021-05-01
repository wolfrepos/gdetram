package io.github.oybek.gdetram.dao.impl

import cats.effect.{ContextShift, IO}
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.scalatest.funsuite.AnyFunSuite

trait PostgresSetup extends ForAllTestContainer {
  this: AnyFunSuite =>

  override val container: PostgreSQLContainer = PostgreSQLContainer()
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  lazy val transactor: Transactor.Aux[IO, Unit] =
    Transactor
      .fromDriverManager[IO](
        container.driverClassName,
        container.jdbcUrl,
        container.username,
        container.password
      )

  override def afterStart(): Unit = {
    val flyway = Flyway
      .configure()
      .dataSource(container.jdbcUrl, container.username, container.password)
      .load()
    flyway.clean()
    flyway.migrate()
  }
}
