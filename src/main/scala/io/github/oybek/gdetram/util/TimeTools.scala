package io.github.oybek.gdetram.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import cats.syntax.all._
import cats.effect.Sync

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object TimeTools {
  implicit class PF[F[_]: Sync: Timer](ff: F[Unit]) {
    def everyDayAt(h: Int, m: Int): F[Unit] = {
      for {
        scheduleTime <- Sync[F].delay { LocalDateTime.now.withHour(h).withMinute(m) }
        now <- Sync[F].delay { LocalDateTime.now }
        sleepBeforeStart = now.until(
          if (now.isBefore(scheduleTime)) scheduleTime else scheduleTime.plusDays(1),
          ChronoUnit.SECONDS
        )
        _ <- Timer[F].sleep(sleepBeforeStart.seconds)
        _ <- every(1.day)
      } yield ()
    }

    def every(finiteDuration: FiniteDuration, fromTo: (Int, Int)): F[Unit] =
      for {
        _ <- ff
        now <- Sync[F].delay { LocalDateTime.now }
        curHour = now.getHour
        _ <- Timer[F].sleep(
          if (curHour >= fromTo._1 && curHour <= fromTo._2)
            finiteDuration
          else
            now.until(
              now.plusDays(1).withHour(fromTo._1),
              ChronoUnit.SECONDS
            ).seconds
        )
        _ <- every(finiteDuration, fromTo)
      } yield ()

    private def every(finiteDuration: FiniteDuration): F[Unit] =
      for {
        _ <- ff
        _ <- Timer[F].sleep(finiteDuration)
        _ <- every(finiteDuration)
      } yield ()
  }
}
