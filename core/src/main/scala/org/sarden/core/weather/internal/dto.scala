package org.sarden.core.weather.internal

import doobie.{Read, Write}

case class WeatherMeasurementDTO(
    collectedAt: Long,
    temperature: Double,
    source: String,
) derives Read,
      Write
