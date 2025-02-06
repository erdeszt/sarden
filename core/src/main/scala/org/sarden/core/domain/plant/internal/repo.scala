package org.sarden.core.domain.plant.internal

import doobie.implicits.given
import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.domain.plant.*

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): URIO[Connection, Vector[Plant]]
  def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Connection, PlantId]

case class LivePlantRepo(idGenerator: IdGenerator) extends PlantRepo:

  override def searchPlants(
      filter: SearchPlantFilters,
  ): URIO[Connection, Vector[Plant]] =
    tzio {
      sql"SELECT id, name FROM plant".query[Plant].to[Vector]
    }.orDie

  override def createPlant(
      name: PlantName,
      details: PlantDetails,
  ): URIO[Connection, PlantId] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.instant
      _ <- tzio {
        sql"INSERT INTO plant (${id.toString}, ${name.unwrap}, ${now.getEpochSecond})".update.run
      }.orDie
    yield PlantId(id)
