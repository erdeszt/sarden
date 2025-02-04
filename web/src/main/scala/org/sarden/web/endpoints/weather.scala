package org.sarden.web.endpoints

import java.time.Instant

import sttp.tapir.json.upickle.*
import sttp.tapir.ztapir.*
import sttp.tapir.{Codec, CodecFormat, Mapping, Schema}
import upickle.default.ReadWriter
import zio.*

import org.sarden.core.domain.weather.*
import org.sarden.web.*

given Schema[Temperature] = Schema.anyObject
given Schema[SensorId] = Schema.anyObject
given Schema[WeatherMeasurement] = Schema.derived
given Schema[EmptyResponse] = Schema.derived
given Codec[String, SensorId, CodecFormat.TextPlain] =
  Codec.string.map(Mapping.from(SensorId(_))(_.unwrap))

case class EmptyResponse() derives ReadWriter

val addWeatherMeasurementEndpoint = endpoint.post
  .in("weather")
  .in(jsonBody[Vector[WeatherMeasurement]])
  .out(jsonBody[EmptyResponse])

val getWeatherMeasurementsEndpoint = endpoint.get
  .in("weather")
  .in(query[Option[Instant]]("from"))
  .in(query[Option[Instant]]("to"))
  .in(query[Option[SensorId]]("sensor_id"))
  .out(jsonBody[Vector[WeatherMeasurement]])

def weatherEndpoints: List[AppServerEndpoint] =
  List(
    addWeatherMeasurementEndpoint.zServerLogic { measurements =>
      ZIO.serviceWithZIO[WeatherService](
        _.addMeasurements(measurements).as(EmptyResponse()),
      )
    },
    getWeatherMeasurementsEndpoint.zServerLogic { (from, to, source) =>
      ZIO.serviceWithZIO[WeatherService](
        _.getMeasurements(GetMeasurementsFilters(from, to, source)),
      )
    },
  )
