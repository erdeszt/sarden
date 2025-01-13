package org.sarden.core.domain.weather

import java.time.Instant

import upickle.default.ReadWriter

import org.sarden.core.json.javatime.given

case class WeatherMeasurement(
    collectedAt: Instant,
    temperature: Temperature,
    source: SensorId,
) derives ReadWriter

opaque type Temperature = Double

object Temperature:
  def apply(raw: Double): Temperature = raw

  given ReadWriter[Temperature] =
    upickle.default.readwriter[Double].bimap(value => value, json => json)

opaque type SensorId = String

extension (sensorId: SensorId) def unwrap: String = sensorId

object SensorId:
  def apply(raw: String): SensorId = raw

  given ReadWriter[SensorId] =
    upickle.default.readwriter[String].bimap(value => value, json => json)

case class GetMeasurementsFilters(
    from: Option[Instant],
    to: Option[Instant],
    sensorId: Option[SensorId],
)
