package io.github.oybek.gdetram.service

import java.io.File
import java.sql.Timestamp

import cats.syntax.all._
import cats.effect.Sync
import io.github.oybek.gdetram.db.repository.{DailyMetric, JournalRepoAlg, UserRepoAlg}
import io.github.oybek.gdetram.domain.Platform
import io.github.oybek.gdetram.util.Chart

trait MetricServiceAlg[F[_]] {
  def mainMetrics: F[(File, String)]
  def platformPie: F[(File, String)]
  def cityPie: F[(File, String)]
}

class MetricService[F[_]: Sync](implicit
                                journalRepo: JournalRepoAlg[F],
                                userRepo: UserRepoAlg[F]) extends MetricServiceAlg[F] {

  def cityPie: F[(File, String)] =
    userRepo.selectCityUserCount.flatMap {
      data =>
        val sum = data.values.sum
        val dataPrepared = data.map(x => (x._1.name, 100f * x._2 / sum))
        val caption = data.map { case (city, count) =>
          s"${city.name}: $count"
        }.mkString("\n")
        Chart.pieChart(dataPrepared).map(_ -> caption)
    }

  def platformPie: F[(File, String)] =
    userRepo.selectPlatformUserCount.flatMap {
      data =>
        val sum = data.values.sum
        val dataPrepared = data.map(x => (Platform.toEnum(x._1), 100f * x._2 / sum))
        val caption = data.map { case (platform, count) =>
          s"${Platform.toEnum(platform)}: $count"
        }.mkString("\n")
        Chart.pieChart(dataPrepared).map(_ -> caption)
    }

  def mainMetrics: F[(File, String)] =
    for {
      cityDailyMetrics <- journalRepo.selectAllDailyMetrics
      globalDailyMetrics = cityDailyMetrics
        .groupBy(_.dateWhen)
        .map { case (dateWhen, dailyMetrics) =>
          dailyMetrics.foldLeft(DailyMetric(dateWhen, "all", 0, 0)) {
            case (acc, cur) =>
              acc.copy(
                active = acc.active + cur.active,
                passive = acc.passive + cur.passive
              )
          }
        }.toList
      chart <- Chart.timeMetric(globalDailyMetrics)
      caption = cityDailyMetrics
        .groupBy(_.cityName)
        .toList
        .flatMap { case (_, cityMetrics) => cityReport(cityMetrics) }
        .mkString("\n")
    } yield (chart, caption)

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
    }
  }
}
