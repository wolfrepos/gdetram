package io.github.oybek.gdetram.db

import cats.effect.IO
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway

trait TestTx {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val transactor = {
    Flyway
      .configure()
      .dataSource("jdbc:postgresql://localhost:5432/test", "test", "test")
      .load()
      .migrate()

    Transactor
      .fromDriverManager[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/test",
        "test",
        "test"
      )
  }
}
