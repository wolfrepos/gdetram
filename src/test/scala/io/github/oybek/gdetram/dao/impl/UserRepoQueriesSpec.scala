package io.github.oybek.gdetram.dao.impl

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.model.{Record, User}
import io.github.oybek.gdetram.service.impl.UserServiceImpl
import io.github.oybek.gdetram.service.model.Message.Geo
import org.scalatest.funsuite.AnyFunSuite

import java.sql.Timestamp

class UserRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("checks UserRepoQueries") {
    check(UserRepoImpl.insert(User((Tg, 1), 1, None, 0)))
    check(UserRepoImpl.select((Tg, 1)))
    check(UserRepoImpl.selectAll)
    check(UserRepoImpl.updateq(User((Tg, 1), 1, None, 0)))
    check(UserServiceImpl.refreshUserInfoQ)
  }
}
