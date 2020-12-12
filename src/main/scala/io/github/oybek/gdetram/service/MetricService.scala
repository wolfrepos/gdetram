package io.github.oybek.gdetram.service

import java.io.File
import java.sql.Timestamp
import cats.syntax.all._
import cats.effect.Sync
import io.github.oybek.gdetram.db.repository.UserRepoAlg
import io.github.oybek.gdetram.domain.model.Platform._
import io.github.oybek.gdetram.domain.model.UserInfo

import java.time.LocalDateTime

trait MetricServiceAlg[F[_]] {
  def userStats: F[String]
}

class MetricService[F[_]: Sync](implicit userRepo: UserRepoAlg[F]) extends MetricServiceAlg[F] {

  def userStats: F[String] =
    for {
      usersInfo <- userRepo.selectUsersInfo
      caption = usersInfo
        .groupBy(_.user.platform)
        .view.mapValues(cityReport)
        .map {
          case (Vk, info) => s"ВК\n$info"
          case (Tg, info) => s"Телега\n$info"
        }
        .mkString("\n\n")
    } yield caption

  implicit def ordered: Ordering[Timestamp] = (x: Timestamp, y: Timestamp) => x compareTo y

  private def cityReport(usersInfo: List[UserInfo]): String =
    usersInfo
      .groupBy(_.user.city)
      .map {
        case (city, usersInfo) =>
          val (active, passive) = usersInfo.foldLeft((0, 0)) {
            case ((a, p), userInfo) =>
              if (userInfo.lastWriteTime.after(2.weeksAgo)) (a + 1, p)
              else (a, p + 1)
          }
          s"#${city.name} - Активных: $active, Пассивных: $passive"
      }.mkString("\n")

  implicit class IntOps(n: Int) {
    def weeksAgo: Timestamp =
      Timestamp.valueOf(LocalDateTime.now().minusWeeks(n))
  }
}
