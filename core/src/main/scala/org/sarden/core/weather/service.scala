package org.sarden.core.weather

import zio.*

import org.sarden.core.tx.*
import org.sarden.core.weather.internal.{LiveWeatherRepo, WeatherRepo}

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
