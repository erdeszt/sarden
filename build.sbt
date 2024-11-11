ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.1"

lazy val baseSettings = Seq(
  scalacOptions ++= Seq(
    "-encoding",
    "utf8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:noAutoTupling",
    "-language:strictEquality",
    "-Wunused:all",
    "-Wshadow:all",
    "-Wvalue-discard",
    "-Wsafe-init",
    "-Yexplicit-nulls",
    // TODO: Werror on CI
    // TODO: Check some real world projects for more lint flags
  )
)

lazy val core = (project in file("core"))
  .settings(baseSettings*)
  .settings(
    name := "sarden-core",
    libraryDependencies ++= Seq(
    ),
  )

lazy val cli = (project in file("cli"))
  .settings(baseSettings*)
  .settings(
    name := "sarden-cli",
    libraryDependencies ++= Seq(

    ),
  ).dependsOn(core)

lazy val web = (project in file("web"))
  .settings(baseSettings*)
  .settings(
    name := "sarden-web",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.11.8",
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.11.8",
      "com.softwaremill.ox" %% "core" % "0.5.1"
    ),
  ).dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, cli, web)
  .settings(
    name := "sarden",
  )
