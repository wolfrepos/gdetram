package io.github.oybek.gdetram.donors

import com.danielasfregola.randomdatagenerator.RandomDataGenerator._
import io.github.oybek.gdetram.model.{City, Record, Stop, User}
import io.github.oybek.gdetram.service.UserId
import io.github.oybek.gdetram.service.model.Message
import io.github.oybek.gdetram.service.model.Message.{Geo, Text}
import org.scalacheck.Gen.chooseNum
import org.scalacheck.{Arbitrary, Gen}

import java.sql.Timestamp

object TestDonors {
  val randomCity        = random[City]
  val randomGeoMessage  = random[Geo]
  val randomMessage     = random[Message]
  val randomRecord      = random[Record]
  val randomStop        = random[Stop]
  val randomTextMessage = random[Text]
  val randomUser        = random[User]
  val randomUserId      = random[UserId]

  implicit lazy val arbitraryTimestamp: Arbitrary[Timestamp] =
    chooseNum(
      Timestamp.valueOf("2020-01-01 00:00:00").getTime,
      Timestamp.valueOf("2030-01-01 00:00:00").getTime
    ).map(new Timestamp(_)).arb

  implicit class genOps[A](val gen: Gen[A]) extends AnyVal {
    def arb: Arbitrary[A] = Arbitrary(gen)
  }
}
