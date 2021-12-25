package io.github.oybek.gdetram.service.impl

import cats.implicits._
import cats.{Monad, ~>}
import io.github.oybek.gdetram.dao.{CityRepo, UserRepo}
import io.github.oybek.gdetram.service.Replies._
import io.github.oybek.gdetram.model.{Message, _}
import io.github.oybek.gdetram.model.Message.{Geo, Text}
import io.github.oybek.gdetram.service.{Handler, Reply, UserId}

class RegistrationService[F[_]: Monad, G[_]: Monad](implicit
                                                   cityRepo: CityRepo[G],
                                                   userRepo: UserRepo[G],
                                                   transaction: G ~> F) extends Handler[F, (UserId, Message), User] {
  override val handle: ((UserId, Message)) => F[Either[Reply, User]] = {
    case (userId, message) =>
      transaction {
        userRepo.get(userId).flatMap {
          case Some(user) => user.asRight[Reply].pure[G]
          case None => message match {
            case geo: Geo => detectCityByGeo(userId, geo)
            case Text(text) => detectCityByName(userId, text)
          }
        }
      }
  }

  private def detectCityByName(userId: (Platform, Long), text: String) = {
    for {
      (city, mistakeNum) <- cityRepo.findByName(text)
      cities <- cityRepo.getAll
      replyOrUser <- if (mistakeNum > 4) {
        cantFindCity(cities.map(_.name)).asLeft[User].pure[G]
      } else {
        userRepo.add(User(userId, city.id)).as(
          cityChosen(city.name, cities.map(_.name)).asLeft[User]
        )
      }
    } yield replyOrUser
  }

  private def detectCityByGeo(userId: (Platform, Long), geo: Geo) = {
    for {
      city <- cityRepo.getNearest(geo)
      user = User(userId, city.id)
      _ <- userRepo.add(User(userId, city.id))
    } yield user.asRight[Reply]
  }
}
