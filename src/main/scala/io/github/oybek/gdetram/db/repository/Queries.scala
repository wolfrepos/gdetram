package io.github.oybek.gdetram.db.repository

import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.implicits._
import doobie.implicits.javasql._
import io.github.oybek.gdetram.domain.model.{City, Platform, PsMessage, Record, Stop, User}

object Queries {

  def selectAllCitites: Query0[String] =
    sql"""
         |SELECT name from city
         |""".stripMargin.query[String]

  def selectMostMatchedCity(query: String): Query0[(City, Int)] =
    sql"""
         |SELECT *, levenshtein(LOWER(name), LOWER($query)) from city order by levenshtein limit 1
         |""".stripMargin.query[(City, Int)]

  def upsertUserCity(platform: Platform, userId: Int, cityId: Int): Update0 =
    sql"""
         |INSERT INTO usr(platform, id, city_id) VALUES($platform, $userId, $cityId)
         |ON CONFLICT(platform, id) DO UPDATE SET city_id = $cityId
         |""".stripMargin.update

  def selectUser(platform: Platform, userId: Int): Query0[User] =
    sql"""
         |select platform, usr.id, city_id, name, latitude, longitude
         |  from usr
         |  left join city
         |  on usr.city_id = city.id
         |  where usr.platform = $platform and usr.id = $userId
         |""".stripMargin.query[User]

  def selectMostMatched(query: String, cityId: Int): Query0[(Stop, Int)] =
    sql"""
         |select stop.id,
         |       stop.name,
         |       stop.latitude,
         |       stop.longitude,
         |       stop.url,
         |       city.id,
         |       city.name,
         |       city.latitude,
         |       city.longitude,
         |       levenshtein(LOWER(stop.name), LOWER($query))
         |  from stop
         |  left join city on stop.city_id = city.id
         |  where city_id = $cityId
         |  order by levenshtein
         |  limit 1
         |""".stripMargin.query[(Stop, Int)]

  def selectNearest(latitude: Float,
                    longitude: Float): Query0[Stop] =
    sql"""
         |select stop.id,
         |       stop.name,
         |       stop.latitude,
         |       stop.longitude,
         |       stop.url,
         |       city.id,
         |       city.name,
         |       city.latitude,
         |       city.longitude
         |  from stop
         |  left join city on stop.city_id = city.id
         |  order by (stop.latitude - $latitude)^2 + (stop.longitude - $longitude)^2 limit 3;
         |""".stripMargin.query[Stop]

  def insertRecordSql(record: Record): Update0 = {
    val Record(stopId, time, userId, text, platform) = record
    sql"insert into journal values ($stopId, $time, $userId, $text, $platform)".update
  }

  def getAsyncMessageFor(user: (Platform, Long)): Query0[String] =
    sql"select text from async_message where platform = ${user._1} and id = ${user._2} limit 1".query[String]

  def delAsyncMessageFor(user: (Platform, Long), text: String): Update0 =
    sql"delete from async_message where platform = ${user._1} and id = ${user._2} and text = $text".update

  def getSyncMessage: Query0[(Platform, Long, String)] =
    sql"select platform, id, text from sync_message limit 1".query[(Platform, Long, String)]

  def delSyncMessageFor(user: (Platform, Long), text: String): Update0 =
    sql"delete from sync_message where platform = ${user._1} and id = ${user._2} and text = $text".update
}
