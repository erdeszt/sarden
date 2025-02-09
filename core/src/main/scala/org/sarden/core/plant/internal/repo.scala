package org.sarden.core.plant.internal

import io.scalaland.chimney.dsl.*
import neotype.interop.chimney.given
import neotype.interop.doobie.given
import zio.*

import org.sarden.core.*
import org.sarden.core.plant.*
import org.sarden.core.tx.*
import org.sarden.core.ulid.given

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): URIO[Tx, Vector[Plant]]
  def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Tx, PlantId]

case class LivePlantRepo(idGenerator: IdGenerator) extends PlantRepo:

  override def searchPlants(
      filter: SearchPlantFilters,
  ): URIO[Tx, Vector[Plant]] =
    Tx {
      sql"SELECT id, name FROM plant".queryThrough[PlantDTO, Plant].to[Vector]
    }

  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Tx, PlantId] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.instant
      _ <- Tx {
        sql"""INSERT INTO plant
             |(id, name, created_at)
             |VALUES
             |(${id}, ${name}, ${now.getEpochSecond})""".stripMargin.update.run
      }
    yield PlantId(id)
