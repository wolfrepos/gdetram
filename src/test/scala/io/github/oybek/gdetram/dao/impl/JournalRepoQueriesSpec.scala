package io.github.oybek.gdetram.dao.impl

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.model.Record
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp

class JournalRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check JournalRepo queries") {
    check(JournalRepoImpl.insertRecordSql(Record(1, new Timestamp(System.currentTimeMillis()), "", "", Tg)))
  }
}
