package org.sarden.core.domain.weather.internal

import doobie.Update
import doobie.implicits.given
import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.domain.weather.*

private[weather] trait WeatherRepo:
  def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): URIO[Connection, Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): URIO[Connection, Vector[WeatherMeasurement]]

class LiveWeatherRepo extends WeatherRepo:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): URIO[Connection, Unit] =
    tzio {
      Update[WeatherMeasurement](
        """INSERT INTO weather_measurement
           |(collected_at, temperature, sensor_id)
           |VALUES (?, ?, ?)""".stripMargin,
      ).updateMany(measurements)
    }.orDie.unit

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): URIO[Connection, Vector[WeatherMeasurement]] =
    tzio {
      sql"SELECT * FROM weather_measurement"
        .query[WeatherMeasurement]
        .to[Vector]
    }.orDie
