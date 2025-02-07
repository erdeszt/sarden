package org.sarden.core.weather.internal

import zio.*

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
      Tx.Bulk[WeatherMeasurement](
        """INSERT INTO weather_measurement
           |(collected_at, temperature, sensor_id)
           |VALUES (?, ?, ?)""".stripMargin,
      ).updateMany(measurements)
    }.unit

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): URIO[Tx, Vector[WeatherMeasurement]] =
    Tx {
      sql"SELECT * FROM weather_measurement"
        .query[WeatherMeasurement]
        .to[Vector]
    }
