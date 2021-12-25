package io.github.oybek.gdetram.service

import cats.Id
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import io.github.oybek.gdetram.donors.TestDonors.randomGeoMessage
import io.github.oybek.gdetram.model.Button.GeoButton
import io.github.oybek.gdetram.model.Message.Text
import io.github.oybek.gdetram.service.impl.StartService
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StartServiceSpec extends AnyFlatSpec with Matchers {
  "StartService" should "handle 'начать', '/start' messages" in {
    startService.handle(Text("начать")) shouldBe (
      """Привет!
        |Подскажи в каком ты городе?
        |Или просто отправь геопозицию""".stripMargin,
      List(List(GeoButton))
    ).asLeft[Unit].pure[Id]
  }

  it should "ignore the rest" in {
    startService.handle(randomGeoMessage) shouldBe ().asRight[Reply].pure[Id]
    startService.handle(Text(random[String])) shouldBe ().asRight[Reply].pure[Id]
  }

  private lazy val startService = new StartService[Id]
}
