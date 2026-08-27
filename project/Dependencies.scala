import sbt.*

object Dependencies {

  lazy val core: Seq[ModuleID] = Seq(
    bcrypt,
    neotype,
    ulid,
    scalatest,
  )

  lazy val web: Seq[ModuleID] = Seq(
    contextualLogging,
    handlebars,
    logbackCore,
    logbackClassic,
    neotype,
    neotypePureconfig,
    pureconfig,
    scalatest,
    slf4j,
    upickle,
    vertx,
    vertxJunit,
    vertxWeb,
  )

  val bcrypt = "at.favre.lib" % "bcrypt" % "0.10.2"

  val contextualLogging =
    "io.reactiverse" % "reactiverse-contextual-logging-logback" % "2.1.0"

  val handlebars = "com.github.jknack" % "handlebars" % "4.5.4"

  val logbackCore = "ch.qos.logback" % "logback-core" % Versions.logback
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logback

  val neotype = "io.github.kitlangton" %% "neotype" % Versions.neotype

  val neotypePureconfig =
    "io.github.kitlangton" %% "neotype-pureconfig" % Versions.neotype

  val pureconfig = "com.github.pureconfig" %% "pureconfig-core" % "0.17.10"

  val scalatest = "org.scalatest" %% "scalatest-funspec" % "3.2.20" % Test

  val slf4j = "org.slf4j" % "slf4j-api" % "2.0.18"

  val ulid = "com.github.f4b6a3" % "ulid-creator" % "5.2.4"

  val upickle = "com.lihaoyi" %% "upickle" % "4.4.3"

  val vertx = "io.vertx" % "vertx-core" % Versions.vertx
  val vertxJunit = "io.vertx" % "vertx-junit5" % Versions.vertx
  val vertxWeb = "io.vertx" % "vertx-web" % Versions.vertx

}

object Versions {

  val logback = "1.6.3"
  val neotype = "0.7.0"
  val vertx = "5.1.6"

}
