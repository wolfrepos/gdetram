package io.github.oybek.gdetram.config

import io.github.oybek.gdetram.config.Config.DatabaseConfig
import io.github.oybek.vk4s.api.GetLongPollServerReq
import pureconfig.ConfigReader.Result
import pureconfig._
import pureconfig.generic.auto._

case class Config(getLongPollServerReq: GetLongPollServerReq,
                  database: DatabaseConfig,
                  tgBotApiToken: String,
                  adminTgIds: String)

object Config {
  def load: Result[Config] =
    ConfigSource.default.load[Config]

  case class DatabaseConfig(driver: String,
                            url: String,
                            user: String,
                            password: String)
}
