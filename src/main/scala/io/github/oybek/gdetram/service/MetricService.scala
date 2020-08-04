package io.github.oybek.gdetram.service

import java.io.File
import java.sql.Timestamp

import cats.syntax.all._
import cats.effect.Sync
import io.github.oybek.gdetram.db.repository.{DailyMetric, JournalRepoAlg, UserRepoAlg}
import io.github.oybek.gdetram.domain.model.Platform

trait MetricServiceAlg[F[_]] {
  def mainMetrics: F[String]
}

class MetricService[F[_]: Sync](implicit
                                journalRepo: JournalRepoAlg[F],
                                userRepo: UserRepoAlg[F]) extends MetricServiceAlg[F] {

  def mainMetrics: F[String] =
    for {
      cityDailyMetrics <- journalRepo.selectAllDailyMetrics
      caption = cityDailyMetrics
        .groupBy(_.cityName)
        .toList
        .flatMap { case (_, cityMetrics) => cityReport(cityMetrics) }
        .mkString("\n")
    } yield caption

  implicit def ordered: Ordering[Timestamp] = (x: Timestamp, y: Timestamp) => x compareTo y

  private def cityReport(cityMetrics: List[DailyMetric]): Option[String] = {
    cityMetrics
      .sortBy(_.dateWhen)(Ordering[Timestamp].reverse)
      .take(2) match {
      case Nil => None
      case DailyMetric(_, cityName, active, passive)::Nil =>
        s"#$cityName - Активных: $active Пассивных: $passive".some
      case x::y::Nil =>
        val ad = x.active - y.active
        val pd = x.passive - y.passive
        def ss(x: Int) = if (x >= 0) s"+$x" else s"$x"
        s"#${x.cityName} - Активных: ${x.active}(${ss(ad)}) Пассивных: ${x.passive}(${ss(pd)})".some
      case _ => "".some
    }
  }
}
