import sbt.Keys._
import sbt._

object Compiler {

  val settings = Seq(
    scalaVersion := "2.13.2",
    scalacOptions ++= options,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
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