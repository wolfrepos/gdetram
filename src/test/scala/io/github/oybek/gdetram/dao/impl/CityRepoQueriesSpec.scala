package io.github.oybek.gdetram.dao.impl

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.service.model.Message.Geo
import org.scalatest.funsuite.AnyFunSuite

class CityRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check CityRepo queries") {
    check(CityRepoImpl.select(1))
    check(CityRepoImpl.selectAll)
    check(CityRepoImpl.selectMostMatched("name"))
    check(CityRepoImpl.selectNearest(Geo(0.0f, 0.0f)))
  }
}
