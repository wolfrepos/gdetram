import sbt.Keys._
import sbt._

object Compiler {

  val settings = Seq(
    scalaVersion := "2.13.2",
    scalacOptions ++= options,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)
  )

  lazy val options = Seq(
    "-encoding", "utf8",
    "-Xfatal-warnings",
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  )
}