package io.github.oybek.gdetram.db.repository

import cats.effect.Sync
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.github.oybek.gdetram.domain.City

trait CityRepoAlg[F[_]] {
  def selectCity(query: String): F[(City, Int)]
  def selectAllCitiesNames: F[List[String]]
}

class CityRepo[F[_]: Sync](transactor: Transactor[F]) extends CityRepoAlg[F] {
  def selectCity(query: String): F[(City, Int)] =
    Queries
      .selectMostMatchedCity(query)
      .unique
      .transact(transactor)

  def selectAllCitiesNames: F[List[String]] =
    Queries.selectAllCitites.to[List].transact(transactor)
}
