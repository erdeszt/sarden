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
  ),
)

lazy val core = (project in file("core"))
  .settings(baseSettings *)
  .settings(
    name := "sarden-core",
    libraryDependencies ++= Seq(
      "com.github.f4b6a3" % "ulid-creator" % Versions.ulid,
      "com.lihaoyi" %% "upickle" % Versions.upickle,
      "dev.zio" %% "izumi-reflect" % Versions.izumiReflect,
      "org.flywaydb" % "flyway-core" % Versions.flyway,
      "org.xerial" % "sqlite-jdbc" % Versions.sqlite,
      "org.scalikejdbc" %% "scalikejdbc" % Versions.scalikeJdbc,
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test,
      "org.scalatest" %% "scalatest-flatspec" % Versions.scalaTest % Test,
    ),
  )

lazy val web = (project in file("web"))
  .settings(baseSettings *)
  .settings(
    name := "sarden-web",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % Versions.logback,
      "com.lihaoyi" %% "scalatags" % Versions.scalaTags,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-json-upickle" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % Versions.tapir,
      "com.softwaremill.ox" %% "core" % Versions.ox,
    ),
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, web)
  .settings(
    name := "sarden",
  )
