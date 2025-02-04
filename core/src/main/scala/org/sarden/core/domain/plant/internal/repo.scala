package org.sarden.core.domain.plant.internal

import com.github.f4b6a3.ulid.Ulid
import scalikejdbc.*
import zio.*

import org.sarden.core.domain.plant.*

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): UIO[Vector[Plant]]

case class LivePlantRepo() extends PlantRepo:
  override def searchPlants(filter: SearchPlantFilters): UIO[Vector[Plant]] =
    ZIO.attemptBlocking {
      DB.autoCommit { implicit session =>
        sql"SELECT * FROM plant"
          .map { row =>
            Plant(
              PlantId(Ulid.from(row.string("id"))),
              PlantName(row.string("name")),
              PlantDetails(),
            )
          }
          .list
          .apply()
          .toVector
      }
    }.orDie
