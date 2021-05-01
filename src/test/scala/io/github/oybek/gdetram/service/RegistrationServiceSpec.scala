package io.github.oybek.gdetram.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, catsSyntaxOptionId}
import cats.{Id, ~>}
import io.github.oybek.gdetram.dao.{CityRepo, UserRepo}
import io.github.oybek.gdetram.donors.TestDonors.{randomCity, randomGeoMessage, randomMessage, randomTextMessage, randomUser, randomUserId}
import io.github.oybek.gdetram.model.User
import io.github.oybek.gdetram.service.impl.RegistrationService
import io.github.oybek.gdetram.service.model.Button.GeoButton
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RegistrationServiceSpec extends AnyFlatSpec with Matchers with MockFactory {

  it should "return user if it exists" in {
    (mockUserRepo.get _)
      .expects(randomUserId)
      .returns(randomUser.some.pure[Id])
      .once()

    registrationService.handle(randomUserId, randomMessage) shouldBe randomUser.asRight[Reply].pure[Id]
  }

  it should "create user if it doesn't exist and geo is given" in {
    val user = User(randomUserId, randomCity.id)

    inSequence {
      (mockUserRepo.get _)
        .expects(randomUserId)
        .returns(Option.empty[User].pure[Id])
        .once()

      (mockCityRepo.getNearest _)
        .expects(randomGeoMessage)
        .returns(randomCity)
        .once()

      (mockUserRepo.add _)
        .expects(user)
        .once()
    }

    registrationService.handle(randomUserId, randomGeoMessage) shouldBe user.asRight[Reply].pure[Id]
  }

  it should "create user if it doesn't exist and there is city found by name" in {
    val user = User(randomUserId, randomCity.id)

    inSequence {
      (mockUserRepo.get _)
        .expects(randomUserId)
        .returns(Option.empty[User].pure[Id])
        .once()

      (mockCityRepo.findByName _)
        .expects(randomTextMessage.text)
        .returns((randomCity, 3).pure[Id])
        .once()

      (() => mockCityRepo.getAll)
        .expects()
        .returns(List(randomCity).pure[Id])

      (mockUserRepo.add _)
        .expects(user)
        .once()
    }

    registrationService.handle(randomUserId, randomTextMessage) shouldBe (
      s"""🏙️ Выбран город ${randomCity.name}
         |
         |Для смены города напиши 'город', а потом название города, например:
         |город Казань
         |Или просто отправь геопозицию
         |
         |Доступные города:
         |${randomCity.name}""".stripMargin,
      List(List(GeoButton))
    ).asLeft[User].pure[Id]
  }

  it should "reply that city not found" in {
    inSequence {
      (mockUserRepo.get _)
        .expects(randomUserId)
        .returns(Option.empty[User].pure[Id])
        .once()

      (mockCityRepo.findByName _)
        .expects(randomTextMessage.text)
        .returns((randomCity, 5).pure[Id])
        .once()

      (() => mockCityRepo.getAll)
        .expects()
        .returns(List(randomCity).pure[Id])
    }

    registrationService.handle(randomUserId, randomTextMessage) shouldBe (
      s"""Не нашел такой город 😟
         |Попробуй еще раз
         |Или просто отправь геопозицию
         |
         |Доступные города: ${randomCity.name}""".stripMargin,
      List(List(GeoButton))
    ).asLeft[User].pure[Id]
  }

  // set up
  private implicit lazy val transaction: ~>[Id, Id] = new ~>[Id, Id] {
    override def apply[A](a: Id[A]): Id[A] = a
  }
  private implicit lazy val mockCityRepo = mock[CityRepo[Id]]
  private implicit lazy val mockUserRepo = mock[UserRepo[Id]]
  private lazy val registrationService = new RegistrationService[Id, Id]
}
