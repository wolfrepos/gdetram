package io.github.oybek.gdetram.dao.impl

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.samples.TestInstances.randomGeo
import org.scalatest.funsuite.AnyFunSuite

class CityRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check CityRepo queries") {
    check(CityRepoImpl.select(1))
    check(CityRepoImpl.selectAll)
    check(CityRepoImpl.selectMostMatched("name"))
    check(CityRepoImpl.selectNearest(randomGeo))
  }
}
