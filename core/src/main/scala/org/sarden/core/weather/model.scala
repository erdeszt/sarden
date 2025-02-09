package org.sarden.core.weather

import java.time.OffsetDateTime

import neotype.*

case class WeatherMeasurement(
    collectedAt: OffsetDateTime,
    temperature: Temperature,
    source: SensorId,
)

type Temperature = Temperature.Type
object Temperature extends Newtype[Double]

// TODO: Ulid
type SensorId = SensorId.Type
object SensorId extends Newtype[String]

case class GetMeasurementsFilters(
    from: Option[OffsetDateTime],
    to: Option[OffsetDateTime],
    sensorId: Option[SensorId],
)
