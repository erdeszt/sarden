package org.sarden.core.domain.weather

import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.domain.weather.internal.*

trait WeatherService:
  def addMeasurements(measurements: Vector[WeatherMeasurement]): UIO[Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[Vector[WeatherMeasurement]]

object WeatherService:
  val live: URLayer[Database, WeatherService] =
    ZLayer.fromFunction(LiveWeatherService(LiveWeatherRepo(), _))

class LiveWeatherService(repo: WeatherRepo, db: Database)
    extends WeatherService:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): UIO[Unit] =
    repo.addMeasurements(measurements)

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[Vector[WeatherMeasurement]] =
    repo.getMeasurements(filters)
