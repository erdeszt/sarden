package org.sarden.web.routes.api

import java.time.OffsetDateTime

import io.scalaland.chimney.dsl.*
import neotype.interop.chimney.given
import sttp.tapir.Schema
import sttp.tapir.json.zio.*
import sttp.tapir.ztapir.*
import zio.*
import zio.json.JsonCodec

import org.sarden.core.SystemErrors.DataInconsistencyError
import org.sarden.core.time.given
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
        measurements <- ZIO
          .foreach(measurementDtos) { dto =>
            // TODO: Extract this pattern
            ZIO.fromEither(
              dto
                .transformIntoPartial[WeatherMeasurement]
                .asEither
                .left
                .map(DataInconsistencyError(_)),
            )
          }
          .orDie
        _ <- ZIO.serviceWithZIO[WeatherService]:
          _.addMeasurements(measurements)
      yield EmptyResponse()
    },
    getWeatherMeasurementsEndpoint.zServerLogic { (fromRaw, toRaw, sourceRaw) =>
      for
        from <- ZIO
          .foreach(fromRaw) { raw =>
            ZIO.fromEither(
              raw
                .transformIntoPartial[OffsetDateTime]
                .asEither
                .left
                .map(DataInconsistencyError(_)),
            )
          }
          .orDie
        to <- ZIO
          .foreach(toRaw) { raw =>
            ZIO.fromEither(
              raw
                .transformIntoPartial[OffsetDateTime]
                .asEither
                .left
                .map(DataInconsistencyError(_)),
            )
          }
          .orDie
        source = sourceRaw.map(SensorId(_))
        measurements <- ZIO.serviceWithZIO[WeatherService]:
          _.getMeasurements(GetMeasurementsFilters(from, to, source)).map(
            _.map(_.transformInto[WeatherMeasurementDTO]),
          )
      yield measurements
    },
  )
