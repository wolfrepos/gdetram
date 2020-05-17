package io.github.oybek.gdetram

import java.sql.Timestamp

import cats.effect.IO
import io.github.oybek.gdetram.db.repository.DailyMetric
import io.github.oybek.gdetram.util.Chart
import org.scalatest.{FlatSpec, Matchers}

class ChartTest extends FlatSpec with Matchers {

  "chart" must "draw" in {
    val metrics = List(
      DailyMetric(Timestamp.valueOf("2020-03-14 23:59:26.380097"), "ekb", 1296, 4656),
      DailyMetric(Timestamp.valueOf("2020-03-13 23:59:26.380097"), "ekb", 954, 0),
      DailyMetric(Timestamp.valueOf("2020-03-15 23:59:26.412645"), "ekb", 1340, 4633),
      DailyMetric(Timestamp.valueOf("2020-03-16 23:59:13.292128"), "ekb", 1728, 4598),
      DailyMetric(Timestamp.valueOf("2020-03-17 23:59:53.061932"), "ekb", 1880, 4565),
      DailyMetric(Timestamp.valueOf("2020-03-18 23:59:27.521468"), "ekb", 1934, 4545),
    )

    Chart.timeMetric[IO](metrics).unsafeRunSync()
  }
}
