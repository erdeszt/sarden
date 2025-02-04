package org.sarden.core.domain.weather

import zio.*

import org.sarden.core.domain.weather.internal.*

trait WeatherService:
  def addMeasurements(measurements: Vector[WeatherMeasurement]): UIO[Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[List[WeatherMeasurement]]

class LiveWeatherService(repo: WeatherRepo) extends WeatherService:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): UIO[Unit] =
    repo.addMeasurements(measurements)

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[List[WeatherMeasurement]] =
    repo.getMeasurements(filters)
