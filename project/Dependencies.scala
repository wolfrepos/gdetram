import sbt._

object Dependencies {

  object V {
    val catsCore = "2.0.0"
    val catsEffect = "2.0.0"
    val circe = "0.12.1"
    val scalaTest = "3.0.5"
    val http4s = "0.20.0"
    val slf4j = "1.7.26"
    val logback = "1.2.3"
    val uPickle = "0.7.1"
    val pureConfig = "0.10.2"
    val flyway = "5.2.4"
    val doobie = "0.8.8"
    val jsoup = "1.7.2"
    val telegramium = "1.0.0-RC1"
    val mock = "4.4.0"
    val mockTest = "3.1.0"
    val scalaChart = "0.5.1"
  }

  val catsCore = "org.typelevel" %% "cats-core" % V.catsCore
  val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
  val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % Test
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % V.pureConfig
  val flyway = "org.flywaydb" % "flyway-core" % V.flyway
  val jsoup = "org.jsoup" % "jsoup" % V.jsoup
  val scalaChart = "com.github.wookietreiber" %% "scala-chart" % V.scalaChart

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % V.doobie,
    "org.tpolecat" %% "doobie-postgres" % V.doobie,
    "org.tpolecat" %% "doobie-hikari" % V.doobie,
    "org.tpolecat" %% "doobie-h2" % V.doobie,
    "org.tpolecat" %% "doobie-scalatest" % V.doobie % Test
  )

  val circe = Seq(
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-parser" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-generic-extras" % V.circe
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-circe" % V.http4s,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s
  )

  val logger = Seq(
    "org.slf4j" % "slf4j-api" % V.slf4j,
    "ch.qos.logback" % "logback-classic" % V.logback
  )

  val uPickle = Seq(
    "com.lihaoyi" %% "upickle" % V.uPickle,
    "com.lihaoyi" %% "upack" % V.uPickle
  )

  val telegramium = Seq(
    "io.github.apimorphism" %% "telegramium-core" % V.telegramium,
    "io.github.apimorphism" %% "telegramium-high" % V.telegramium
  )

  val mock = Seq(
    "org.scalamock" %% "scalamock" % V.mock % Test,
    "org.scalatest" %% "scalatest" % V.mockTest % Test
  )

  val common = Seq(catsCore, catsEffect, scalaTest, pureConfig, flyway, jsoup, scalaChart) ++
    telegramium ++
    circe ++
    http4s ++
    logger ++
    uPickle ++
    doobie ++
    mock
}
