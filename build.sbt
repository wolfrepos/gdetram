
ThisBuild / version := "0.1"
ThisBuild / organization := "io.github.oybek"

val settings = Compiler.settings ++ Seq()

lazy val plato = ProjectRef(
  file("plato/"),
  "plato"
)

lazy val vk4s = RootProject(uri("git://github.com/oybek/vk4s.git"))

lazy val gdetram = (project in file("."))
  .settings(name := "gdetram")
  .settings(libraryDependencies ++= Dependencies.common)
  .settings(sonarProperties := Sonar.properties)
  .dependsOn(plato, vk4s)
