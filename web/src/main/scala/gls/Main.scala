package gls

import java.nio.file.{Files, Paths}

import io.vertx.core.*
import org.slf4j.LoggerFactory
import pureconfig.ConfigSource

object Main {

  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    mainW(args)
  }

  def mainC(args: Array[String]): Unit = {
    val raw = Files.readString(Paths.get("c:\\Users\\erdeszt\\Desktop\\Novenyek.txt"))
    val (plants, errors) = gls.domain.plant.parse(raw)

    if (errors.nonEmpty) {
      println(errors.mkString("\n"))
    } else {
      println(s"No errors")
    }

    println(s"${plants.length} plants have been parsed")

    println(gls.domain.plant.render(plants))

    println(
      s"All rosaceae:\n${gls.domain.plant.render(plants.filter(_.family == gls.domain.plant.Family("Rosaceae")))}",
    )
  }

  def mainW(args: Array[String]): Unit = {
    ConfigSource.default.load[AppConfig] match {
      case Left(error) =>
        logger.error(s"Failed to load configuration ${error}")
      case Right(appConfig) =>
        val vertx = Vertx.vertx()
        val app = AppVerticle(appConfig)

        vertx
          .deployVerticle(
            app,
            DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD),
          )
          .await()
        ()
    }
  }

}
