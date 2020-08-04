
ThisBuild / version := "0.1"
ThisBuild / organization := "io.github.oybek"

lazy val plato = ProjectRef(
  file("plato/"),
  "plato"
)

lazy val vk4s = ProjectRef(uri("https://github.com/oybek/vk4s.git#master"), "vk4s")

lazy val gdetram = (project in file("."))
  .settings(name := "gdetram")
  .settings(libraryDependencies ++= Dependencies.common)
  .settings(sonarProperties := Sonar.properties)
  .settings(Compiler.settings)
  .dependsOn(plato, vk4s)

assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", xs@_*) => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}