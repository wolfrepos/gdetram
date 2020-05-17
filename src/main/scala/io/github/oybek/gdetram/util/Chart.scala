package io.github.oybek.gdetram.util

import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat

import cats.syntax.all._
import cats.effect.Sync
import cats.instances.option._
import io.github.oybek.gdetram.db.repository.DailyMetric
import scalax.chart.module.Charting

object Chart extends Charting {

  private val `dd.MM` = new SimpleDateFormat("dd.MM")

  implicit def ordered: Ordering[Timestamp] = (x: Timestamp, y: Timestamp) => x compareTo y

  def pieChart[F[_]: Sync](data: Map[String, Float]): F[java.io.File] = {
    val path = data.hashCode().toString + ".png"
    Sync[F].delay {
      PieChart(data.toPieDataset).saveAsPNG(path, (1366, 768))
    }.as(new java.io.File(path))
  }

  def timeMetric[F[_]: Sync](cityMetrics: List[DailyMetric], n: Int = 7): F[java.io.File] = {
    val cityMetricsSorted = cityMetrics.sortBy(_.dateWhen)(Ordering[Timestamp]).takeRight(n)
    val xs = cityMetricsSorted.map(_.dateWhen)
    val ys = List(
      "Пассивных" -> cityMetricsSorted.map(_.passive),
      "Активных" -> cityMetricsSorted.map(_.active),
    )
    val fileName = cityMetricsSorted.hashCode().toString + ".png"
    Chart.genCategoryChart(xs, ys)(fileName)
  }

  private def genCategoryChart[F[_]: Sync](xs: List[Timestamp], ys: List[(String, List[Int])], resolution: (Int, Int) = (1366, 768))(path: String): F[File] =
    Sync[F].delay {
      val dataSet = ys.map {
        case (label, ys) =>
          label -> (xs.map(`dd.MM`.format) zip ys)
      }.toMap.toCategoryDataset
      AreaChart(dataSet, stacked = true).saveAsPNG(path, resolution)
    }.as(new java.io.File(path))

}
