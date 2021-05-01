package io.github.oybek.gdetram.donors

import com.danielasfregola.randomdatagenerator.RandomDataGenerator._
import io.github.oybek.gdetram.model.{Record, Stop, User}
import io.github.oybek.gdetram.service.UserId
import io.github.oybek.gdetram.service.model.Message.Geo
import org.scalacheck.Gen.chooseNum
import org.scalacheck.{Arbitrary, Gen}

import java.sql.Timestamp

object TestDonors {
  val randomGeo = random[Geo]
  val randomUser = random[User]
  val randomUserId = random[UserId]
  val randomRecord = random[Record]
  val randomStop = random[Stop]

  implicit lazy val arbitraryTimestamp: Arbitrary[Timestamp] =
    chooseNum(
      Timestamp.valueOf("2020-01-01 00:00:00").getTime,
      Timestamp.valueOf("2030-01-01 00:00:00").getTime
    ).map(new Timestamp(_)).arb

  implicit class genOps[A](val gen: Gen[A]) extends AnyVal {
    def arb: Arbitrary[A] = Arbitrary(gen)
  }
}
