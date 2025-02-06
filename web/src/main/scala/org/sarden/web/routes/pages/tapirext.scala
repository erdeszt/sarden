package org.sarden.web.routes.pages

import java.nio.charset.StandardCharsets

import scalatags.Text
import sttp.tapir.*

def htmlViewCodec[T: Schema](
    renderer: T => Text.TypedTag[String],
): Codec[String, T, CodecFormat.TextHtml] =
  Codec.anyString(CodecFormat.TextHtml())(_ =>
    throw new Exception("HTML input body is not supported"),
  )(value => renderer(value).render)

def htmlView[T: Schema](
    renderer: T => Text.TypedTag[String],
): EndpointIO.Body[String, T] =
  EndpointIO.Body(
    RawBodyType.StringBody(StandardCharsets.UTF_8),
    htmlViewCodec(renderer),
    EndpointIO.Info.empty,
  )

val baseEndpoint = sttp.tapir.ztapir.endpoint
