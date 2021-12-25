package io.github.oybek.gdetram.service.impl

import cats.effect.Bracket
import cats.implicits._
import cats.{Monad, ~>}
import io.github.oybek.gdetram.dao.{CityRepo, MessageRepo, UserRepo}
import io.github.oybek.gdetram.model.User
import io.github.oybek.gdetram.service.{Handler, Reply}

class StatusService[F[_]: Bracket[*[_], Throwable], G[_]: Monad](implicit
                                                                 messageRepo: MessageRepo[G],
                                                                 cityRepo: CityRepo[G],
                                                                 userRepo: UserRepo[G],
                                                                 transaction: G ~> F) extends Handler[F, User, String] {

  override val handle: User => F[Either[Reply, String]] = {
    user =>
      transaction {
        for {
          messageOpt <- messageRepo.pollAsyncMessage(user.userId)
          city <- cityRepo.get(user.cityId)
          isVip = user.lastMonthActiveDays >= 10
        } yield (
          s"${if (isVip) "⭐ " else ""}${city.name}" +
          s"\n${messageOpt.getOrElse("")}"
        ).asRight[Reply]
      }
  }
}
