package io.github.oybek.gdetram.dao

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.model.{Record, User}
import io.github.oybek.gdetram.donors.TestDonors.randomGeoMessage
import io.github.oybek.gdetram.service.impl.UserServiceImpl
import io.github.oybek.gdetram.service.model.Message.Geo
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp

class StopRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("checks StopRepo queries") {
    check(StopRepoImpl.selectMostMatched("123", 1))
    check(StopRepoImpl.selectNearest(randomGeoMessage))
  }
}
