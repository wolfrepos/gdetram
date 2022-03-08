
ThisBuild / version := "0.1"
ThisBuild / organization := "io.github.oybek"

lazy val plato = ProjectRef(
  file("plato/"),
  "plato"
)

lazy val vkontaktum = ProjectRef(uri("https://github.com/oybek/vkontaktum.git#master"), "vkontaktum")

lazy val gdetram = (project in file("."))
  .settings(name := "gdetram")
  .settings(libraryDependencies ++= Dependencies.common)
  .settings(libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-simple")) })
  .settings(sonarProperties := Sonar.properties)
  .settings(Compiler.settings)
  .dependsOn(plato, vkontaktum)