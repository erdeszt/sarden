package org.sarden.core.domain.weather

import org.sarden.core.domain.weather.internal.*

trait WeatherService:
  def addMeasurements(measurements: Vector[WeatherMeasurement]): Unit
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): List[WeatherMeasurement]

class LiveWeatherService(repo: WeatherRepo) extends WeatherService:

  override def addMeasurements(measurements: Vector[WeatherMeasurement]): Unit =
    repo.addMeasurements(measurements)

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): List[WeatherMeasurement] =
    repo.getMeasurements(filters)
