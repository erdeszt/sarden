package org.sarden.core.domain.weather

import zio.*

import org.sarden.core.domain.weather.internal.*
import org.sarden.core.tx.*

trait WeatherService:
  def addMeasurements(measurements: Vector[WeatherMeasurement]): UIO[Unit]
  def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[Vector[WeatherMeasurement]]

object WeatherService:
  val live: URLayer[Tx.Runner, WeatherService] =
    ZLayer.fromFunction(LiveWeatherService(LiveWeatherRepo(), _))

class LiveWeatherService(repo: WeatherRepo, tx: Tx.Runner)
    extends WeatherService:

  override def addMeasurements(
      measurements: Vector[WeatherMeasurement],
  ): UIO[Unit] =
    tx.runOrDie(repo.addMeasurements(measurements))

  override def getMeasurements(
      filters: GetMeasurementsFilters,
  ): UIO[Vector[WeatherMeasurement]] =
    tx.runOrDie(repo.getMeasurements(filters))
