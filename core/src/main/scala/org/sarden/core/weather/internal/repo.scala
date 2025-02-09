package org.sarden.core.weather.internal

import io.scalaland.chimney.dsl.*
import neotype.interop.chimney.given
import zio.*

import org.sarden.core.time.given
import org.sarden.core.tx.*
import org.sarden.core.weather.*

private[weather] trait WeatherRepo:
  def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): URIO[Tx, Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): URIO[Tx, Vector[WeatherMeasurement]]

class LiveWeatherRepo extends WeatherRepo:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): URIO[Tx, Unit] =
    Tx {
      Tx.Bulk[WeatherMeasurementDTO](
        """INSERT INTO weather_measurement
           |(collected_at, temperature, sensor_id)
           |VALUES (?, ?, ?)""".stripMargin,
      ).updateMany(measurements.map(_.transformInto[WeatherMeasurementDTO]))
    }.unit

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): URIO[Tx, Vector[WeatherMeasurement]] =
    Tx {
      sql"SELECT * FROM weather_measurement"
        .queryThrough[WeatherMeasurementDTO, WeatherMeasurement]
        .to[Vector]
    }
