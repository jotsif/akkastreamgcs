import sbt._

object Dependencies {
  lazy val scalatestversion = "3.0.1"
  lazy val akka_version = "2.4.17"
  lazy val akka_http_version = "10.0.5"

  lazy val akka_http = "com.typesafe.akka" %% "akka-http" % akka_http_version
  lazy val scalaTest = "org.scalatest" %% "scalatest" % scalatestversion % Test
//  lazy val jwt = "io.igl" %% "jwt" % "1.2.0"
  lazy val spray_json = "io.spray" %%  "spray-json" % "1.3.3"
}
