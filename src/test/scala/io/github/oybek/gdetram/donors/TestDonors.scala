package io.github.oybek.gdetram.donors

import cats.data.NonEmptyList
import com.danielasfregola.randomdatagenerator.RandomDataGenerator._
import io.github.oybek.gdetram.model.{City, Record, Stop, User}
import io.github.oybek.gdetram.service.UserId
import io.github.oybek.gdetram.service.model.Message
import io.github.oybek.gdetram.service.model.Message.{Geo, Text}
import org.scalacheck.Gen.chooseNum
import org.scalacheck.{Arbitrary, Gen}

import java.sql.Timestamp

object TestDonors {
  val randomCity: City                 = random[City]
  val randomCities: NonEmptyList[City] = NonEmptyList.fromListUnsafe(random[City](100).toList.distinctBy(_.id))
  val randomGeoMessage: Geo            = random[Geo]
  val randomMessage: Message           = random[Message]
  val randomRecord: Record             = random[Record]
  val randomStop: Stop                 = random[Stop]
  val randomTextMessage: Text          = random[Text]
  val randomUser: User                 = random[User]
  val randomUserId: UserId             = random[UserId]

  implicit lazy val arbitraryFloat: Arbitrary[Float] =
    chooseNum(-100f, +100f).arb

  implicit lazy val arbitraryTimestamp: Arbitrary[Timestamp] =
    chooseNum(
      Timestamp.valueOf("2020-01-01 00:00:00").getTime,
      Timestamp.valueOf("2030-01-01 00:00:00").getTime
    ).map(new Timestamp(_)).arb

  implicit class genOps[A](val gen: Gen[A]) extends AnyVal {
    def arb: Arbitrary[A] = Arbitrary(gen)
  }
}
