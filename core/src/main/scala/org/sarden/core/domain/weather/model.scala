package org.sarden.core.domain.weather

import java.time.Instant

import doobie.{Get, Put, Read, Write}
import zio.json.*

//import org.sarden.core.json.javatime.given

case class WeatherMeasurement(
    collectedAt: Instant,
    temperature: Temperature,
    source: SensorId,
) derives JsonCodec,
      Read,
      Write

opaque type Temperature = Double

object Temperature:
  def apply(raw: Double): Temperature = raw

  given Get[Temperature] = Get[Double].map(raw => raw)
  given Put[Temperature] = Put[Double].contramap(raw => raw)
  given JsonEncoder[Temperature] = JsonEncoder[Double].contramap(raw => raw)
  given JsonDecoder[Temperature] = JsonDecoder[Double].map(raw => raw)

opaque type SensorId = String

extension (sensorId: SensorId) def unwrap: String = sensorId

object SensorId:
  def apply(raw: String): SensorId = raw

  given Get[SensorId] = Get[String].map(raw => raw)
  given Put[SensorId] = Put[String].contramap(raw => raw)
  given JsonEncoder[SensorId] = JsonEncoder[String].contramap(raw => raw)
  given JsonDecoder[SensorId] = JsonDecoder[String].map(raw => raw)

given instantGet: Get[Instant] =
  Get[Long].map(raw => Instant.ofEpochSecond(raw))
given instantPut: Put[Instant] =
  Put[Long].contramap(instant => instant.getEpochSecond)

case class GetMeasurementsFilters(
    from: Option[Instant],
    to: Option[Instant],
    sensorId: Option[SensorId],
)
