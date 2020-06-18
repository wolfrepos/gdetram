package io.github.oybek.gdetram.db

import java.sql.{DriverManager, Timestamp}

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.scalatest.IOChecker
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import io.github.oybek.gdetram.db.repository.{JournalRepo, Queries, UserRepo}
import io.github.oybek.gdetram.domain.model.Platform.{Tg, Vk}
import io.github.oybek.gdetram.domain.model
import io.github.oybek.gdetram.domain.model.{PsMessage, Record}
import org.flywaydb.core.Flyway
import org.scalatest.{FunSuite, Matchers}

class QueriesSpec extends FunSuite with IOChecker with ForAllTestContainer {

  override val container = PostgreSQLContainer()
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  override def afterStart(): Unit = {
    val flyway = Flyway
      .configure()
      .dataSource(container.jdbcUrl, container.username, container.password)
      .load()
    flyway.clean()
    flyway.migrate()
  }

  lazy val transactor = {
    Transactor
      .fromDriverManager[IO](
        container.driverClassName,
        container.jdbcUrl,
        container.username,
        container.password
      )
  }

  test("user repo queries") {
    check(UserRepo.selectCityUserCount)
  }

  test("check metrics query") {
    check(JournalRepo.doDailyMetricsDump)
  }

  test("city select") {
    check(Queries.selectMostMatchedCity("Екатеринург"))
  }

  test("all city select") {
    check(Queries.selectAllCitites)
  }

  test("usr upsert/select queries") {
    check(Queries.upsertUserCity(Vk, 1, 1))
    check(Queries.upsertUserCity(Vk, 1, 1))
    check(Queries.selectUser(Vk, 1))
  }

  test("Journal insert request") {
    check(
      Queries.insertRecordSql(Record(0, new Timestamp(3L), "0", "test", Vk))
    )
    check(
      Queries.insertRecordSql(model.Record(0, new Timestamp(3L), "0", "test", Tg))
    )
  }

  test("Select most matched stops") {
    check(Queries.selectMostMatched("Ельцина", 1))
    check(Queries.selectNearest(0.0f, 0.0f))
  }

  test("Message queries") {
    check(Queries.getAsyncMessageFor((Vk, 123)))
    check(Queries.getSyncMessage)
    check(Queries.delSyncMessageFor((Vk, 123)))
    check(Queries.delAsyncMessageFor((Vk, 123)))
  }
}
