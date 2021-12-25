package io.github.oybek.gdetram.dao

import doobie.scalatest.IOChecker
import io.github.oybek.gdetram.dao.impl.MessageRepoImpl
import io.github.oybek.gdetram.model.Platform.Tg
import io.github.oybek.gdetram.donors.TestDonors.randomUserId
import org.scalatest.funsuite.AnyFunSuite

class MessageRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check MessageRepo queries") {
    check(MessageRepoImpl.delAsyncMessageFor(randomUserId, ""))
    check(MessageRepoImpl.getAsyncMessageFor(randomUserId))
    check(MessageRepoImpl.getSyncMessage(Tg, 1))
    check(MessageRepoImpl.delSyncMessageFor(randomUserId, ""))
  }
}
