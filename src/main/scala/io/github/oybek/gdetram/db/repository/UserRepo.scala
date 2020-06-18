package io.github.oybek.gdetram.db.repository

import cats.syntax.all._
import cats.effect.Sync
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import doobie.implicits._
import doobie.util.query.Query0
import io.github.oybek.gdetram.domain.model.{City, Platform, User}

trait UserRepoAlg[F[_]] {
  def upsert(user: User): F[Int]
  def selectUser(platform: Platform, userId: Int): F[Option[User]]
  def selectPlatformUserCount: F[Map[Platform, Int]]
  def selectCityUserCount: F[Map[City, Long]]
}

class UserRepo[F[_]: Sync](transactor: Transactor[F]) extends UserRepoAlg[F] {

  def selectCityUserCount: F[Map[City, Long]] =
    UserRepo
      .selectCityUserCount
      .to[List]
      .transact(transactor)
      .map(_.toMap)

  def selectPlatformUserCount: F[Map[Platform, Int]] =
    UserRepo
      .selectPlatformUserCountQ
      .to[List]
      .transact(transactor)
      .map(_.toMap)

  def upsert(user: User): F[Int] =
    Queries
      .upsertUserCity(user.platform, user.id, user.city.id)
      .run
      .transact(transactor)

  def selectUser(platform: Platform, userId: Int): F[Option[User]] =
    Queries.selectUser(platform, userId).option.transact(transactor)
}

object UserRepo {
  val selectPlatformUserCountQ: Query0[(Platform, Int)] =
    sql"select platform, count(*) from usr group by platform".query[(Platform, Int)]

  val selectCityUserCount: Query0[(City, Long)] =
    sql"""
         |select city.id,
         |       city.name,
         |       city.latitude,
         |       city.longitude,
         |       user_count
         |  from city left join (
         |    select city_id,
         |           coalesce(count(*), 0) as user_count
         |      from usr
         |      group by city_id
         |  ) as t1 on city.id = city_id where user_count is not null
         |""".stripMargin.query[(City, Long)]
}
