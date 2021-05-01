package io.github.oybek.gdetram.service

import cats.Id
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import com.danielasfregola.randomdatagenerator.RandomDataGenerator.random
import io.github.oybek.gdetram.donors.TestDonors.{randomGeo, randomUserId}
import io.github.oybek.gdetram.service.impl.StartService
import io.github.oybek.gdetram.service.model.Button.GeoButton
import io.github.oybek.gdetram.service.model.Message.Text
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StartServiceSpec extends AnyFlatSpec with Matchers {
  "StartService" should "handle 'начать', '/start' messages" in {
    startService.handle(randomUserId, Text("начать")) shouldBe (
      """Привет!
        |Подскажи в каком ты городе?
        |Или просто отправь геопозицию""".stripMargin,
      List(List(GeoButton))
    ).asLeft[Unit].pure[Id]
  }

  it should "ignore the rest" in {
    startService.handle(randomUserId, randomGeo) shouldBe ().asRight[Reply].pure[Id]
    startService.handle(randomUserId, Text(random[String])) shouldBe ().asRight[Reply].pure[Id]
  }

  private lazy val startService = new StartService[Id]
}
