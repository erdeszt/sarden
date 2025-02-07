package org.sarden.core.plant.internal

import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.plant.*
import org.sarden.core.tx.*

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
      sql"SELECT id, name FROM plant".query[Plant].to[Vector]
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
             |(${id.toString}, ${name.unwrap}, ${now.getEpochSecond})""".stripMargin.update.run
      }
    yield PlantId(id)
