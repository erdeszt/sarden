package org.sarden.web.routes.api

import java.time.Instant

import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import sttp.tapir.{Codec, Schema}
import zio.*
import zio.json.JsonCodec

import org.sarden.core.weather.*
import org.sarden.web.*
import org.sarden.web.routes.schemas.weather.given

given Schema[EmptyResponse] = Schema.derived

case class EmptyResponse() derives JsonCodec

val addWeatherMeasurementEndpoint = baseEndpoint.post
  .in("weather")
  .in(jsonBody[Vector[WeatherMeasurement]])
  .out(jsonBody[EmptyResponse])

val getWeatherMeasurementsEndpoint = baseEndpoint.get
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
