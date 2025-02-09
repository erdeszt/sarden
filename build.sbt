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
    "-Xkind-projector:underscores",
  ),
)

lazy val core = (project in file("core"))
  .settings(baseSettings *)
  .settings(
    name := "sarden-core",
    libraryDependencies ++= Seq(
      "com.github.f4b6a3" % "ulid-creator" % Versions.ulid,
      "dev.zio" %% "izumi-reflect" % Versions.izumiReflect,
      "dev.zio" %% "zio" % Versions.zio,
      "dev.zio" %% "zio-json" % Versions.zioJson,
      "org.flywaydb" % "flyway-core" % Versions.flyway,
      "org.xerial" % "sqlite-jdbc" % Versions.sqlite,
      "io.github.gaelrenoux" %% "tranzactio-doobie" % Versions.tranzactio,
      "io.github.kitlangton" %% "neotype" % Versions.neotype,
      "io.github.kitlangton" %% "neotype-doobie" % Versions.neotype,
      "io.github.kitlangton" %% "neotype-chimney" % Versions.neotype,
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test" % Versions.zio,
      "dev.zio" %% "zio-test-sbt" % Versions.zio,
    ).map(_ % Test),
  )

lazy val web = (project in file("web"))
  .settings(baseSettings *)
  .settings(
    name := "sarden-web",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % Versions.logback,
      "com.lihaoyi" %% "scalatags" % Versions.scalaTags,
      "com.softwaremill.sttp.tapir" %% "tapir-core" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % Versions.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % Versions.tapir,
    ),
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, web)
  .settings(
    name := "sarden",
  )
