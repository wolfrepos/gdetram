package io.github.oybek.gdetram.config

import io.github.oybek.gdetram.config.Config.DatabaseConfig
import io.github.oybek.vk4s.api.GetLongPollServerReq
import org.scalatest.funsuite.AnyFunSuite

class ConfigSpec extends AnyFunSuite {

  test("Config load") {
    assert(Config.load == Right(expectedConfig))
  }

  private lazy val expectedConfig =
    Config(
      getLongPollServerReq = GetLongPollServerReq(
        groupId = "VK_GROUP_ID",
        accessToken = "VK_ACCESS_TOKEN",
        version = "5.103"
      ),
      database = DatabaseConfig(
        driver = "org.postgresql.Driver",
        url = "DB_URL",
        user = "DB_USER",
        password = "DB_PASS"
      ),
      tgBotApiToken = "TG_BOT_API_TOKEN",
      adminTgIds = "ADMIN_TG_IDS"
    )
}
