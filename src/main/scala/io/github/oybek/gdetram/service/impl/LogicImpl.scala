package io.github.oybek.gdetram.service.impl

import cats.Monad
import cats.data.EitherT
import io.github.oybek.gdetram.model.Message
import io.github.oybek.gdetram.service.{Logic, Reply, UserId}
import io.github.oybek.gdetram.util.Timer

class LogicImpl[F[_]: Monad: Timer, G[_]: Monad](implicit startService: StartService[F],
                                                          registrationService: RegistrationService[F, G],
                                                          cityService: CityService[F, G],
                                                          stopService: StopService[F, G],
                                                          statusService: StatusService[F, G])
  extends Logic[F] {

  def handle(userId: UserId, message: Message): F[Reply] = (
    for {
      _                   <- EitherT(startService.handle(message))
      user                <- EitherT(registrationService.handle(userId, message))
      _                   <- EitherT(cityService.handle(user, message))
      (tabloid, keyboard) <- EitherT(stopService.handle(user, message))
      status              <- EitherT(statusService.handle(user))
    } yield (
      tabloid + "\n" +
        status,
      keyboard
    )
  ).fold(identity, identity)
}
