package io.github.oybek.gdetram.service.impl

import cats.effect.Sync
import cats.implicits._
import cats.{Monad, ~>}
import io.github.oybek.gdetram.dao.{CityRepo, UserRepo}
import io.github.oybek.gdetram.service.Replies._
import io.github.oybek.gdetram.model.{Message, _}
import io.github.oybek.gdetram.model.Message.{Geo, Text}
import io.github.oybek.gdetram.service.{Handler, Reply}

class CityService[F[_]: Sync, G[_]: Monad](implicit
                                           cityRepo: CityRepo[G],
                                           userRepo: UserRepo[G],
                                           transaction: G ~> F) extends Handler[F, (User, Message), Unit] {
  override val handle: ((User, Message)) => F[Either[Reply, Unit]] = {
    case (user, geo: Geo) =>
      transaction {
        for {
          city <- cityRepo.getNearest(geo)
          _ <- userRepo.update(user.copy(cityId = city.id))
        } yield ().asRight[Reply]
      }

    case (user, Text(text)) if text.startsWith("город") =>
      transaction {
        for {
          (city, mistakeNum) <- cityRepo.findByName(text.drop(5).trim)
          cities <- cityRepo.getAll
          replyOrUser <- if (mistakeNum > 4) {
            cantFindCity(cities.map(_.name)).asLeft[Unit].pure[G]
          } else {
            userRepo.update(user.copy(cityId = city.id)).as(
              cityChosen(city.name, cities.map(_.name)).asLeft[Unit]
            )
          }
        } yield replyOrUser
      }

    case _ =>
      ().asRight[Reply].pure[F]
  }
}
