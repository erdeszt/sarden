package org.sarden.web.endpoints

import java.time.Instant

import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.json.upickle.*
import sttp.tapir.ztapir.*
import sttp.tapir.{Codec, CodecFormat, Mapping, Schema}
import upickle.default.ReadWriter

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
  .out(jsonBody[List[WeatherMeasurement]])

def weatherEndpoints(
    service: WeatherService,
): List[ZServerEndpoint[Any, ZioStreams & WebSockets]] =
  List(
    addWeatherMeasurementEndpoint.zServerLogic { measurements =>
      service.addMeasurements(measurements).as(EmptyResponse())
    },
    getWeatherMeasurementsEndpoint.zServerLogic { (from, to, source) =>
      service.getMeasurements(GetMeasurementsFilters(from, to, source))
    },
  )
