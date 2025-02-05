package org.sarden.web.endpoints

import java.nio.charset.StandardCharsets

import scala.io.Source
import scala.util.Using

import sttp.model.{Header, MediaType}
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.web.*

val cssAssetsServerEndpoint: AppServerEndpoint =
  endpoint.get
    .in("assets" / "css" / path[String]("name"))
    .out(sttp.tapir.header(Header.contentType(MediaType.TextCss)))
    .out(stringBody(StandardCharsets.UTF_8))
    .zServerLogic(name =>
      ZIO.attemptBlocking {
        Using(
          Source.fromURL(Main.getClass.getResource(s"/assets/css/${name}")),
        )(
          _.mkString(""),
        ).get
      }.orDie,
    )

val jsAssetsServerEndpoint: AppServerEndpoint =
  endpoint.get
    .in("assets" / "js" / path[String]("name"))
    .out(sttp.tapir.header(Header.contentType(MediaType.TextJavascript)))
    .out(stringBody(StandardCharsets.UTF_8))
    .zServerLogic(name =>
      ZIO.attemptBlocking {
        Using(Source.fromURL(Main.getClass.getResource(s"/assets/js/${name}")))(
          _.mkString(""),
        ).get
      }.orDie,
    )
