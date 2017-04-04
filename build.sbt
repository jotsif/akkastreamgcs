import Dependencies._

lazy val jwt = ProjectRef(build = uri("git://github.com/jotsif/jwt.git#issue_12-support_rs256_with_scope"), project = "jwt")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "akkastreamgcs",
      scalaVersion := "2.11.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "AkkaStreamGCS",
    libraryDependencies ++= Seq(scalaTest, akka_http, spray_json)
  )
  .dependsOn(jwt)
