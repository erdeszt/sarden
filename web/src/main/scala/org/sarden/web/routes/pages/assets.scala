package org.sarden.web.routes.pages

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.io.Source
import scala.util.Using

import sttp.model.{Header, MediaType}
import sttp.tapir.RawBodyType
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.web.*

val cssAssetsServerEndpoint: AppServerEndpoint =
  baseEndpoint.get
    .in("assets" / "css" / path[String]("name"))
    .out(sttp.tapir.header(Header.contentType(MediaType.TextCss)))
    .out(stringBody(StandardCharsets.UTF_8))
    .zServerLogic { name =>
      ZIO.attemptBlocking {
        Using(
          Source.fromURL(Main.getClass.getResource(s"/assets/css/${name}")),
        )(
          _.mkString(""),
        ).get
      }.orDie
    }

val jsAssetsServerEndpoint: AppServerEndpoint =
  baseEndpoint.get
    .in("assets" / "js" / path[String]("name"))
    .out(sttp.tapir.header(Header.contentType(MediaType.TextJavascript)))
    .out(stringBody(StandardCharsets.UTF_8))
    .zServerLogic { name =>
      ZIO.attemptBlocking {
        Using(Source.fromURL(Main.getClass.getResource(s"/assets/js/${name}")))(
          _.mkString(""),
        ).get
      }.orDie
    }

val imageAssetsServerEndpoint: AppServerEndpoint =
  baseEndpoint.get
    .in("assets" / "images" / path[String]("name"))
    .out(sttp.tapir.header(Header.contentType(MediaType.ImageJpeg)))
    .out(byteArrayBody)
    .zServerLogic { name =>
      ZIO.unless(name.endsWith(".jpg"))(
        ZIO.die(
          new RuntimeException("Only .jpg images are supported at the moment."),
        ),
      ) *>
        ZIO.attemptBlocking {
          Files.readAllBytes(
            Paths.get(
              Main.getClass.getResource(s"/assets/images/${name}").getPath,
            ),
          )
        }.orDie
    }
