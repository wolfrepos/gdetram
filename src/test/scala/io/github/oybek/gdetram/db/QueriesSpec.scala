package io.github.oybek.gdetram.db

import java.sql.Timestamp

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.db.repository.{JournalRepo, Queries, UserRepo}
import io.github.oybek.gdetram.domain.Platform.{Tg, Vk}
import io.github.oybek.gdetram.domain.{Record, SpamMessage}
import org.scalatest.{FunSuite, Matchers}

class QueriesSpec extends FunSuite with Matchers with IOChecker with TestTx {

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
      Queries.insertRecordSql(Record(0, new Timestamp(3L), "0", "test", Tg))
    )
  }

  test("Select most matched stops") {
    check(Queries.selectMostMatched("Ельцина", 1))
    check(Queries.selectNearest(0.0f, 0.0f))
  }

  test("Insert message") {
    check(Queries.insertMessageSql(SpamMessage(text = "hello")))
  }

  test("Select not delivered message") {
    check(Queries.getNotDeliveredMessageForSql((Vk, 123)))
  }

  test("Insert to table delivered") {
    check(Queries.markDeliveredForUserSql(123, (Vk, 123)))
  }
}
