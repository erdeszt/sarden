ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.4"

lazy val commonSettings = List(
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:noAutoTupling",
    "-Xkind-projector",
    "-Yexplicit-nulls",
    "-Wsafe-init",
    "-Wvalue-discard",
    "-Wunused:all",
    "-Wnonunit-statement",
    "-experimental",
  ),
)

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "gls-core",
    libraryDependencies ++= Dependencies.core,
  )

lazy val web = (project in file("web"))
  .settings(commonSettings)
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
