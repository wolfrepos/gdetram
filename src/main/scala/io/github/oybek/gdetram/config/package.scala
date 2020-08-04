package io.github.oybek.gdetram

import java.io.File

import cats.effect.Sync
import cats.implicits._
import com.typesafe.config.ConfigFactory
import io.github.oybek.vk4s.model.GetLongPollServerReq
import pureconfig.error.ConfigReaderException

package object config {
  case class DatabaseConfig(driver: String,
                            url: String,
                            user: String,
                            password: String)

  case class Config(getLongPollServerReq: GetLongPollServerReq,
                    database: DatabaseConfig,
                    tgBotApiToken: String,
                    adminTgIds: String)

  object Config {
    import pureconfig.generic.auto._
    import pureconfig._

    def load[F[_]: Sync](configFileName: Option[String]): F[Config] = {
      Sync[F]
        .delay {
          configFileName
            .map(x => ConfigSource.fromConfig(ConfigFactory.parseFile(new File(x))).load[Config])
            .getOrElse(
              ConfigSource.fromConfig(ConfigFactory.load("application.conf")).load[Config]
            )
        }
        .flatMap {
          case Left(e) =>
            Sync[F].raiseError[Config](new ConfigReaderException[Config](e))
          case Right(config) =>
            Sync[F].pure(config)
        }
    }
  }
}
