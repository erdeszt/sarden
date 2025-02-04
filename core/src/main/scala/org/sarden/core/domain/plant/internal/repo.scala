package org.sarden.core.domain.plant.internal

import com.github.f4b6a3.ulid.Ulid
import doobie.implicits.given
import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.domain.plant.*

private[plant] trait PlantRepo:
  def searchPlants(filter: SearchPlantFilters): URIO[Connection, Vector[Plant]]

case class LivePlantRepo() extends PlantRepo:
  override def searchPlants(
      filter: SearchPlantFilters,
  ): URIO[Connection, Vector[Plant]] =
    ???
//    tzio {
//      sql"SELECT * FROM plant".query[Plant].to[List]
//    }.orDie.map(_.toVector) // TODO: .to[Vector]
