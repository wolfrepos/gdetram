package io.github.oybek.gdetram.domain

import java.sql.Timestamp

import cats.effect.{Concurrent, Sync, Timer}
import cats.implicits._
import io.github.oybek.gdetram.db.repository._
import io.github.oybek.gdetram.domain.model.{Button, GeoButton, LinkButton, Platform, Record, Stop, TextButton, User}
import io.github.oybek.gdetram.service.{TabloidAlg, PsServiceAlg}
import io.github.oybek.gdetram.util.Formatting
import io.github.oybek.gdetram.util.vk.Coord

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
                                           psService: PsServiceAlg[F],
                                           stopRepo: StopRepoAlg[F],
                                           userRepo: UserRepoAlg[F])
    extends BrainAlg[F] {

  override def handleText(stateKey: (Platform, Long),
                          text: String): F[(String, List[List[Button]])] =
    for {
      userOpt <- userRepo.selectUser(stateKey._1, stateKey._2.toInt)
      reply <- userOpt match {
        case _ if text.trim.toLowerCase.startsWith("город") =>
          for {
            cityAndMistakeNum <- cityRepo.selectCity(text.trim.drop(5).trim)
            (city, mistakeNum) = cityAndMistakeNum
            res <- if (mistakeNum > 4) {
              Sync[F].pure("Не нашел такой город 😟\nПопробуйте еще раз" -> defaultKeyboard())
            } else {
              userRepo.upsert(User(stateKey._1, stateKey._2.toInt, city)).as(
                s"""
                   |🏛️ Выбран город ${city.name}
                   |
                   |Напишите название нужной Вам остановки
                   |Или отправьте геопозицию - я подскажу
                   |названия ближайших остановок
                   |
                   |Для смены города напишите слово 'город'
                   |а потом название города, например:
                   |город Екатеринбург
                   |город Казань
                   |и т. д.
                   |Или просто отправьте геопозицию
                   |""".stripMargin -> defaultKeyboard(TextButton("город " + city.name))
              )
            }
          } yield res
        case Some(user) => searchStop(stateKey, text, user)
        case None => Sync[F].pure(
          """
            |Подскажите в каком Вы городе?
            |
            |Для этого напишите слово 'город'
            |затем название города, например:
            |город Екатеринбург
            |город Казань
            |и т. д.
            |Ну или просто отправьте геопозицию
            |""".stripMargin -> defaultKeyboard()
        )
      }
    } yield reply

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
              psText <- psService.getNotDeliveredMessageFor(stateKey)
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
      .extractInfo(stop)
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
    List(
      topButton.toList,
      List(
        LinkButton(
          "Вызвать Яндекс.Такси",
          "https://3.redirect.appmetrica.yandex.com/route?ref=2373965&amp;appmetrica_tracking_id=25395763362139037"
        )
      ),
      List(GeoButton)
    )
}
