package io.github.oybek.gdetram.dao.impl

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.model.{Record, User}
import io.github.oybek.gdetram.samples.TestInstances.{randomUser, randomUserId}
import io.github.oybek.gdetram.service.impl.UserServiceImpl
import io.github.oybek.gdetram.service.model.Message.Geo
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp

class UserRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("checks UserRepoQueries") {
    check(UserRepoImpl.insert(randomUser))
    check(UserRepoImpl.select(randomUserId))
    check(UserRepoImpl.selectAll)
    check(UserRepoImpl.updateq(randomUser))
    check(UserServiceImpl.refreshUserInfoQ)
  }
}
