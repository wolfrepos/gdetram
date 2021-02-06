package io.github.oybek.gdetram.util

import telegramium.bots.{Location, Message}

trait TgExtractors {
  object LocationMessage {
    def unapply(msg: Message): Option[Location] =
      msg.location
  }
  object TextMessage {
    def unapply(msg: Message): Option[String] =
      msg.text
  }
}
