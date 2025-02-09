package org.sarden.web.routes.api

import java.time.OffsetDateTime

import io.scalaland.chimney.dsl.*
import sttp.tapir.Schema
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.*
import zio.json.*

import org.sarden.core.mapping.{*, given}
import org.sarden.core.weather.*
import org.sarden.web.*

given Schema[EmptyResponse] = Schema.derived

case class EmptyResponse() derives JsonCodec

private[api] case class WeatherMeasurementDTO(
    collectedAt: Long,
    temperature: Double,
    source: String,
) derives JsonCodec,
      Schema

val addWeatherMeasurementEndpoint = baseEndpoint.post
  .in("weather")
  .in(jsonBody[Vector[WeatherMeasurementDTO]])
  .out(jsonBody[EmptyResponse])

val getWeatherMeasurementsEndpoint = baseEndpoint.get
  .in("weather")
  .in(query[Option[Long]]("from"))
  .in(query[Option[Long]]("to"))
  .in(query[Option[String]]("sensor_id"))
  .out(jsonBody[Vector[WeatherMeasurementDTO]])

def weatherEndpoints: List[AppServerEndpoint] =
  List(
    addWeatherMeasurementEndpoint.zServerLogic { measurementDtos =>
      for
        measurements <- ZIO.foreach(measurementDtos):
          _.transformIntoPartialZIOOrDie[WeatherMeasurement]
        _ <- ZIO.serviceWithZIO[WeatherService]:
          _.addMeasurements(measurements)
      yield EmptyResponse()
    },
    getWeatherMeasurementsEndpoint.zServerLogic { (fromRaw, toRaw, sourceRaw) =>
      for
        from <- ZIO.foreach(fromRaw):
          _.transformIntoPartialZIOOrDie[OffsetDateTime]
        to <- ZIO.foreach(toRaw):
          _.transformIntoPartialZIOOrDie[OffsetDateTime]
        source = sourceRaw.map(SensorId(_))
        measurements <- ZIO.serviceWithZIO[WeatherService]:
          _.getMeasurements(GetMeasurementsFilters(from, to, source)).map:
            _.map(_.transformInto[WeatherMeasurementDTO])
      yield measurements
    },
  )
