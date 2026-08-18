ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.4"

lazy val core = (project in file("core"))
  .settings(
    name := "gls-core",
    libraryDependencies ++= Dependencies.core,
  )

lazy val web = (project in file("web"))
  .settings(
    name := "gls-web",
    libraryDependencies ++= Dependencies.web,
  )
  .dependsOn(core)

lazy val gls = (project in file("."))
  .settings(
    name := "gls",
  )
  .aggregate(core, web)
