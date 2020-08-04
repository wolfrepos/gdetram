package io.github.oybek.gdetram.util

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import telegramium.bots.{Chat, Message}

class TgExtractorsSpec extends AnyFlatSpec with Matchers with TgExtractors {

  "Location extractor" must "extract location" in {
    val location = telegramium.bots.Location(0.0f, 0.0f)
    val message = Message(
      messageId = 0,
      date = 0,
      chat = Chat(0, "123"),
      location = Some(location)
    )
    (message match {
      case Location(location) => Some(location)
      case _                  => None
    }) should be(Some(location))
  }
}
