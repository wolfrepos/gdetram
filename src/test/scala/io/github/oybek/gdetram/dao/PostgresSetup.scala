package io.github.oybek.gdetram.dao

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.scalatest.funsuite.AnyFunSuite
import org.testcontainers.utility.DockerImageName

trait PostgresSetup extends ForAllTestContainer {
  this: AnyFunSuite =>

  override val container: PostgreSQLContainer = PostgreSQLContainer(DockerImageName.parse("postgres:10.10"))

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
