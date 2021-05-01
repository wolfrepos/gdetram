package io.github.oybek.gdetram.util

import com.softwaremill.quicklens._
import io.github.oybek.gdetram.donors.TestDonors.randomStop
import io.github.oybek.plato.model.TransportT.{Bus, Tram}
import io.github.oybek.plato.model.{Arrival, TransportT}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class FormattingSpec
    extends AnyFlatSpec
    with Matchers
    with TgExtractors {

  "Tabloid" must "be formatted with 5 different arrivals" in {
    val stop = randomStop
      .modify(_.name).setTo("Ленина")

    Formatting.toChatText(
      stop,
      "Бажова",
      List(
        Arrival("12", 0 minutes, Tram),
        Arrival("1",  1 minutes, Tram),
        Arrival("25", 5 minutes, Bus),
        Arrival("27", 5 minutes, Bus),
        Arrival("33", 5 minutes, Tram)
      )
    ) shouldBe
      s"""
         |Ленина ${Formatting.rightArrow} Бажова
         |${TransportT.emoji(TransportT.Tram)} 12 подъезжает
         |${TransportT.emoji(TransportT.Tram)} 1 - 1 мин.
         |${TransportT.emoji(TransportT.Tram)} 33 - 5 мин.
         |${TransportT.emoji(TransportT.Bus)} 25, 27 - 5 мин.
         |""".stripMargin
  }

  "Tabloid" must "be formatted with 2 arrivals" in {
    val stop = randomStop
      .modify(_.name).setTo("Ленина")

    Formatting.toChatText(
      stop,
      "Бажова",
      List(
        Arrival("123", 0 minutes, Tram),
        Arrival("123", 1 minutes, Tram)
      )
    ) shouldBe
      s"""
         |Ленина ${Formatting.rightArrow} Бажова
         |${TransportT.emoji(TransportT.Tram)} 123 подъезжает
         |${TransportT.emoji(TransportT.Tram)} 123 - 1 мин.
         |""".stripMargin
  }

  "Tabloid" must "be formatted with no arrivals" in {
    val stop = randomStop
      .modify(_.name).setTo("Ленина")

    Formatting.toChatText(stop, "Бажова", List()) shouldBe
      s"""
         |Ленина ${Formatting.rightArrow} Бажова
         |Ничего не едет
         |""".stripMargin
  }
}
