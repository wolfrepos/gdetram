package io.github.oybek.gdetram.dao

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.dao.impl.JournalRepoImpl
import io.github.oybek.gdetram.donors.TestDonors.randomRecord
import org.scalatest.funsuite.AnyFunSuite

class JournalRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check JournalRepo queries") {
    check(JournalRepoImpl.insertRecordSql(randomRecord))
  }
}
