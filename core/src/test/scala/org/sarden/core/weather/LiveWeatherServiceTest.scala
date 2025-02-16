package org.sarden.core.weather

import zio.*
import zio.test.*

import org.sarden.core.*
import org.sarden.core.mapping.given

object LiveWeatherServiceTest extends BaseSpec:

  override def spec =
    suite("Live WeatherService Test")(
      test("Created measurements should be returned") {
        val temperature = Temperature(21.2)
        val sensorId = SensorId("sensor1")
        for
          weatherService <- ZIO.service[WeatherService]
          now <- zio.Clock.currentDateTime
          _ <- weatherService.addMeasurements(
            Vector(
              WeatherMeasurement(now, temperature, sensorId),
            ),
          )
          measurements <- weatherService.getMeasurements(
            GetMeasurementsFilters.empty,
          )
        yield assertTrue(
          measurements.nonEmpty,
          measurements.head.collectedAt == now,
          measurements.head.temperature == temperature,
          measurements.head.source == sensorId,
        )
      },
    ) @@ TestAspect.before(setupDb) @@ TestAspect.sequential
