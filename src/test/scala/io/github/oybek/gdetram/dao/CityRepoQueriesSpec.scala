package io.github.oybek.gdetram.dao

import cats.Applicative.ops.toAllApplicativeOps
import cats.data.NonEmptyList
import cats.implicits.catsKernelStdOrderForDouble
import doobie.implicits._
import doobie.scalatest.IOChecker
import doobie.{ConnectionIO, Update}
import io.github.oybek.gdetram.donors.TestDonors.{randomCities, randomGeoMessage}
import io.github.oybek.gdetram.model.City
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.util.Random

class CityRepoQueriesSpec extends AnyFunSuite with IOChecker with PostgresSetup {
  test("check queries") {
    check(CityRepoImpl.select(1))
    check(CityRepoImpl.selectAll)
    check(CityRepoImpl.selectMostMatched("name"))
    check(CityRepoImpl.selectNearest(randomGeoMessage))
  }

  test("real queries") {
    (for {
      _ <- prepare
      sampleCity = shuffledRandomCities.head
      _ <- CityRepoImpl.get(sampleCity.id).map(_ shouldBe sampleCity)
      _ <- CityRepoImpl.getAll.map(_.toSet shouldBe shuffledRandomCities.toList.toSet)
      _ <- CityRepoImpl.findByName(sampleCity.name).map { case (city, mistakeNum) =>
        city shouldBe sampleCity
        mistakeNum shouldBe 0
      }
      _ <- CityRepoImpl.getNearest(randomGeoMessage).map { nearestCity =>
        val nearestFromRandomCities = shuffledRandomCities.sortBy { city =>
          Math.pow(city.latitude - randomGeoMessage.latitude, 2) +
            Math.pow(city.longitude - randomGeoMessage.longitude, 2)
        }.head
        nearestCity shouldBe nearestFromRandomCities
      }
    } yield ()).transact(transactor).unsafeRunSync()
  }

  private lazy val shuffledRandomCities = NonEmptyList.fromListUnsafe(
    Random.shuffle(randomCities.toList))

  private def prepare: ConnectionIO[Unit] =
    for {
      _ <- sql"delete from city".update.run
      _ <- Update[City](
        "insert into city (id, name, latitude, longitude) values (?, ?, ?, ?)"
      ).updateMany(randomCities).void
    } yield ()
}
