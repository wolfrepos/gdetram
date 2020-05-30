
ThisBuild / version := "0.1"
ThisBuild / organization := "io.github.oybek"

val settings = Compiler.settings ++ Seq()

lazy val plato = ProjectRef(
  file("plato/"),
  "plato"
)

lazy val gdetram = (project in file("."))
  .settings(name := "gdetram")
  .settings(libraryDependencies ++= Dependencies.common)
  .settings(sonarProperties := Sonar.properties)
  .dependsOn(plato)
