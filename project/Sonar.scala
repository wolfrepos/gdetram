
object Sonar {
  val properties = Map(
    "sonar.host.url" -> "https://sonarcloud.io",
    "sonar.organization" -> "oybek",
    "sonar.projectName" -> "gdetram",
    "sonar.projectKey" -> "oybek_gdetram",
    "sonar.sources" -> "src/main/scala",
    "sonar.tests" -> "src/test/scala",
    "sonar.sourceEncoding" -> "UTF-8",
    "sonar.scala.scoverage.reportPath" -> "target/scala-2.13/scoverage-report/scoverage.xml",
    "sonar.scala.coverage.reportPaths" -> "target/scala-2.13/scoverage-report/scoverage.xml",
  )
}
