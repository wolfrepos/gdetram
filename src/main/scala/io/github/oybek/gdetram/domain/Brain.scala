package io.github.oybek.gdetram.domain

import java.sql.Timestamp
import cats.effect.{Concurrent, Sync, Timer}
import cats.implicits._
import io.github.oybek.gdetram.db.repository._
import io.github.oybek.gdetram.domain.model.{Button, City, GeoButton, LinkButton, Platform, Record, Stop, TextButton, User}
import io.github.oybek.gdetram.service.TabloidAlg
import io.github.oybek.gdetram.util.Formatting
import io.github.oybek.vk4s.domain.Coord

trait BrainAlg[F[_]] {
  def handleText(stateKey: (Platform, Long),
                 text: String): F[(String, List[List[Button]])]
  def handleGeo(stateKey: (Platform, Long),
                coord: Coord): F[(String, List[List[Button]])]
}

class Brain[F[_]: Sync: Concurrent: Timer](implicit
                                           cityRepo: CityRepoAlg[F],
                                           tabloid: TabloidAlg[F],
                                           journalRepo: JournalRepoAlg[F],
                                           messageRepo: MessageRepoAlg[F],
                                           stopRepo: StopRepoAlg[F],
                                           userRepo: UserRepoAlg[F])
    extends BrainAlg[F] with Phrases {

  override def handleText(stateKey: (Platform, Long),
                          text: String): F[(String, List[List[Button]])] =
    for {
      userOpt <- userRepo.selectUser(stateKey._1, stateKey._2.toInt)
      cityNames <- cityRepo.selectAllCitiesNames
      reply <- userOpt match {
        case _ if text.trim.toLowerCase.startsWith("город") =>
          for {
            cityOpt <- cityNameWritten(text.trim.drop(5).trim)
            res <- cityOpt.fold(
              Sync[F].pure("Не нашел такой город 😟\nПопробуйте еще раз" -> defaultKeyboard())
            )(city =>
              userRepo.upsert(User(stateKey._1, stateKey._2.toInt, city)).as(
                cityChosen(city.name, cityNames) -> defaultKeyboard(TextButton("город " + city.name)))
            )
          } yield res
        case Some(user) => searchStop(stateKey, text, user)
        case None =>
          cityNameWritten(text).flatMap {
            case Some(city) =>
              userRepo.upsert(User(stateKey._1, stateKey._2.toInt, city)).as(
                cityChosen(city.name, cityNames) -> defaultKeyboard(TextButton("город " + city.name)))
            case None =>
              (cityAsk(cityNames), defaultKeyboard()).pure[F]
          }
      }
    } yield reply

  private def cityNameWritten(text: String): F[Option[City]] =
    for {
      cityAndMistakeNum <- cityRepo.selectCity(text.trim)
      (city, mistakeNum) = cityAndMistakeNum
      res = if (mistakeNum > 4) { None } else { city.some }
    } yield res

  private def searchStop(stateKey: (Platform, Long),
                         text: String,
                         user: User): F[(String, List[List[Button]])] =
    for {
      stopAndMistakeNumOpt <- stopRepo.selectMostMatched(text, user.city.id)
      res <- stopAndMistakeNumOpt match {
        case Some((stop, mistakeNum)) if mistakeNum < (stop.name.length / 2).max(4) =>
            for {
              tabloidText <- getTabloid(stop)
              currentMillis <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
              _ <- journalRepo.insert(Record(stop.id, new Timestamp(currentMillis), stateKey._2.toString, text, stateKey._1))
              psText <- messageRepo.pollAsyncMessage(stateKey)
              res = tabloidText + psText.map("\n" + _).getOrElse("")
            } yield res -> defaultKeyboard(
              TextButton("город " + user.city.name),
              TextButton(stop.name)
            )
        case Some((_, _)) =>
            Sync[F].pure(
              """
                |Не знаю такую остановку 😟
                |
                |Отправьте геопозицию - я подскажу названия ближайших остановок
                |""".stripMargin -> defaultKeyboard(TextButton("город " + user.city.name)))
        case None =>
          Sync[F].pure(
            s"""
              |Для города ${user.city.name}
              |пока не загружена база остановок
              |""".stripMargin -> defaultKeyboard(TextButton("город " + user.city.name)))
      }
    } yield res

  override def handleGeo(stateKey: (Platform, Long),
                         coord: Coord): F[(String, List[List[Button]])] =
    stopRepo.selectNearest(coord.latitude, coord.longitude).flatMap {
      nearestStops =>
        nearestStops.headOption.traverse {
          stop => userRepo.upsert(User(stateKey._1, stateKey._2.toInt, stop.city))
        }.as(
          s"""
             |Город ${nearestStops.headOption.map(_.city.name).getOrElse("Не определен")}
             |
             |3 ближайшие остановки:
             |${nearestStops.map(x => "- " + x.name).mkString("\n")}
             |""".stripMargin -> nearestStops.map(stop => List(TextButton(stop.name)))
        )
    }

  private def getTabloid(stop: Stop) = {
    tabloid
      .getArrivals(stop)
      .map {
        case Nil => s"На остановку ${stop.name} сейчас ничего не едет"
        case l =>
          l.map {
            case (dir, arrivals) =>
              Formatting.toChatText(stop, dir, arrivals)
          }.mkString
      }
  }

  private def defaultKeyboard(topButton: Button*): List[List[Button]] =
    List(topButton.toList, List(GeoButton))
}
