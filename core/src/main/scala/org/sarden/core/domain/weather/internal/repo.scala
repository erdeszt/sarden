package org.sarden.core.domain.weather.internal

import java.time.Instant

import scalikejdbc.*
import zio.*

import org.sarden.core.domain.weather.*

private[weather] trait WeatherRepo:
  def addMeasurements(measurements: Vector[WeatherMeasurement]): UIO[Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[List[WeatherMeasurement]]

class LiveWeatherRepo extends WeatherRepo:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): UIO[Unit] =
    ZIO
      .attemptBlocking {
        DB.autoCommit { implicit session =>
          sql"""INSERT INTO weather_measurement
           (collected_at, temperature, sensor_id)
           VALUES (?, ?, ?)"""
            .batch(
              measurements.map(measurement =>
                (
                  measurement.collectedAt.getEpochSecond,
                  measurement.temperature,
                  measurement.source.unwrap,
                ),
              ),
            )
            .apply()
        }
      }
      .orDie
      .unit

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[List[WeatherMeasurement]] =
    ZIO.attemptBlocking {
      DB.autoCommit { implicit session =>
        sql"SELECT * FROM weather_measurement"
          .map { row =>
            WeatherMeasurement(
              Instant.ofEpochSecond(row.long("collected_at")),
              Temperature(row.double("temperature")),
              SensorId(row.string("source")),
            )
          }
          .list
          .apply()
      }
    }.orDie
