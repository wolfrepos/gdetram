package io.github.oybek.gdetram.service

import cats.effect.IO
import io.github.oybek.gdetram.db.repository.UserRepoAlg
import io.github.oybek.gdetram.model.Platform.{Tg, Vk}
import io.github.oybek.gdetram.model.{City, User, UserInfo}
import io.github.oybek.gdetram.donnars.StopDonnar
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.sql.Timestamp
import java.time.LocalDateTime

class MetricsServiceSpec extends AnyFlatSpec with Matchers with MockFactory with StopDonnar {
  implicit val userRepo = stub[UserRepoAlg[IO]]
  implicit val metricService = new MetricService[IO]

  private val yekb = City(1, "Екатеринбург", 0.0f, 0.0f)
  private val perm = City(2, "Пермь", 0.0f, 0.0f)

  "userStats method" must "evaluate correct stats" in {
    (() => userRepo.selectUsersInfo)
      .when()
      .returns(IO {
        List(
          UserInfo(User(Vk, 123, yekb), 1.weeksAgo),
          UserInfo(User(Vk, 123, yekb), 5.weeksAgo),
          UserInfo(User(Vk, 123, perm), 5.weeksAgo),
          UserInfo(User(Tg, 123, yekb), 1.weeksAgo),
          UserInfo(User(Tg, 123, yekb), 5.weeksAgo),
          UserInfo(User(Tg, 123, perm), 5.weeksAgo),
          UserInfo(User(Tg, 123, perm), 5.weeksAgo)
        )
      })

    metricService.userStats.unsafeRunSync() shouldBe (
      """ВК
        |#Екатеринбург - Активных: 1, Пассивных: 1
        |#Пермь - Активных: 0, Пассивных: 1
        |
        |Телега
        |#Екатеринбург - Активных: 1, Пассивных: 1
        |#Пермь - Активных: 0, Пассивных: 2""".stripMargin
    )
  }

  implicit class IntOps(n: Int) {
    def weeksAgo: Timestamp =
      Timestamp.valueOf(LocalDateTime.now().minusWeeks(n))
  }
}
