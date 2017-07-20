import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "akkastreamgcs",
      scalaVersion := "2.11.7",
      version      := "0.2.0"
    )),
    name := "AkkaStreamGCS",
    libraryDependencies ++= Seq(scalaTest, akka_http, spray_json, jwt)
  )

