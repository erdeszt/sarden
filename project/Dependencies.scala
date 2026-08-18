import sbt.*

object Dependencies {

  lazy val core = Seq(
    neotype,
  )

  lazy val web = Seq(
    jte,
    logbackCore,
    logbackClassic,
    neotype,
    neotypePureconfig,
    pureconfig,
    slf4j,
    upickle,
    vertx,
    vertxWeb,
  )

  val jte = "gg.jte" % "jte" % "3.2.4"

  val logbackCore = "ch.qos.logback" % "logback-core" % "1.6.3"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.6.3"

  val neotype = "io.github.kitlangton" %% "neotype" % Versions.neotype

  val neotypePureconfig =
    "io.github.kitlangton" %% "neotype-pureconfig" % Versions.neotype

  val pureconfig = "com.github.pureconfig" %% "pureconfig-core" % "0.17.9"

  val slf4j = "org.slf4j" % "slf4j-api" % "2.0.10"

  val upickle = "com.lihaoyi" %% "upickle" % "4.4.2"

  val vertx = "io.vertx" % "vertx-core" % Versions.vertx
  val vertxWeb = "io.vertx" % "vertx-web" % Versions.vertx

}

object Versions {

  val neotype = "0.4.10"
  val vertx = "5.1.6"

}
