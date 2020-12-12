package io.github.oybek.gdetram.db.repository

import cats.syntax.all._
import cats.effect.Sync
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import doobie.implicits._
import doobie.util.query.Query0
import io.github.oybek.gdetram.domain.model._

trait UserRepoAlg[F[_]] {
  def upsert(user: User): F[Int]
  def selectUser(platform: Platform, userId: Int): F[Option[User]]
  def selectUsersInfo: F[List[UserInfo]]
}

class UserRepo[F[_]: Sync](transactor: Transactor[F]) extends UserRepoAlg[F] {
  def upsert(user: User): F[Int] =
    Queries
      .upsertUserCity(user.platform, user.id, user.city.id)
      .run
      .transact(transactor)

  def selectUser(platform: Platform, userId: Int): F[Option[User]] =
    Queries.selectUser(platform, userId).option.transact(transactor)

  def selectUsersInfo: F[List[UserInfo]] =
    Queries
      .selectUsersInfo
      .to[List]
      .transact(transactor)
}