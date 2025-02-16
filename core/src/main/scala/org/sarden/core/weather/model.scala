package org.sarden.core.weather

import java.time.OffsetDateTime

import neotype.*

case class WeatherMeasurement(
    collectedAt: OffsetDateTime,
    temperature: Temperature,
    source: SensorId,
)

type Temperature = Temperature.Type
object Temperature extends Newtype[Double]:
  given CanEqual[Temperature, Temperature] = CanEqual.derived

// TODO: Ulid
type SensorId = SensorId.Type
object SensorId extends Newtype[String]:
  given CanEqual[SensorId, SensorId] = CanEqual.derived

case class GetMeasurementsFilters(
    from: Option[OffsetDateTime],
    to: Option[OffsetDateTime],
    sensorId: Option[SensorId],
)

object GetMeasurementsFilters:
  def empty: GetMeasurementsFilters =
    GetMeasurementsFilters(None, None, None)
