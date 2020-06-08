package io.github.oybek.gdetram.db.repository

import java.sql.Timestamp

import cats.effect.Sync
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import io.github.oybek.gdetram.domain.{Platform, Record}
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.util.query.Query0

case class DailyMetric(dateWhen: Timestamp, cityName: String, active: Int, passive: Int)

trait JournalRepoAlg[F[_]] {
  def insert(record: Record): F[Int]
  def selectAllDailyMetrics: F[List[DailyMetric]]
  def dailyMetricsDump: F[Int]
}

class JournalRepo[F[_]: Sync](transactor: Transactor[F])
    extends JournalRepoAlg[F] {
  def insert(record: Record): F[Int] =
    Queries.insertRecordSql(record).run.transact(transactor)

  def selectAllDailyMetrics: F[List[DailyMetric]] =
    JournalRepo.allDailyMetrics.to[List].transact(transactor)

  def dailyMetricsDump: F[Int] =
    JournalRepo.doDailyMetricsDump.run.transact(transactor)
}

object JournalRepo {
  val doDailyMetricsDump: Update0 =
    sql"""
         |insert into daily_metrics
         |select now(),
         |       city_id,
         |       count(*) filter (where hours_ago <= 14 * 24) as active,
         |       count(*) filter (where hours_ago > 14 * 24) as passive
         |  from (
         |    select EXTRACT(EPOCH FROM (now() - last_active) / 3600) as hours_ago, city_id
         |      from (
         |        select max(time) as last_active, user_id, platform
         |          from journal
         |          group by (user_id, platform)
         |      ) as a1
         |      right join usr on a1.user_id = usr.id::varchar and a1.platform = usr.platform
         |  ) as a2
         |  group by city_id
         |""".stripMargin.update

  val allDailyMetrics: Query0[DailyMetric] = sql"""
         |select date_when, name, active, passive
         |  from daily_metrics
         |  left join city on city_id = city.id
         |  order by date_when
         |""".stripMargin.query[DailyMetric]
}
