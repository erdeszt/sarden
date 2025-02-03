package org.sarden.web.endpoints

import java.time.Instant

import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.json.upickle.*
import sttp.tapir.server.ServerEndpoint
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
): List[ServerEndpoint[Any, Identity]] =
  List(
    addWeatherMeasurementEndpoint.handleSuccess { measurements =>
      service.addMeasurements(measurements)
      EmptyResponse()
    },
    getWeatherMeasurementsEndpoint.handleSuccess { (from, to, source) =>
      service.getMeasurements(GetMeasurementsFilters(from, to, source))
    },
  )
