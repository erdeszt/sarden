package org.sarden.web.routes.schemas

import sttp.tapir.*

import org.sarden.core.domain.weather.*

object weather:
  given Schema[Temperature] = Schema.anyObject
  given Schema[SensorId] = Schema.anyObject
  given Schema[WeatherMeasurement] = Schema.derived
  given Codec[String, SensorId, CodecFormat.TextPlain] =
    Codec.string.map(Mapping.from(SensorId(_))(_.unwrap))
