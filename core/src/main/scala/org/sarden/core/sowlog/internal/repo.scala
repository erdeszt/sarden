package org.sarden.core.sowlog.internal

import java.time.LocalDate

import io.scalaland.chimney.dsl.*
import zio.*
import zio.json.*

import org.sarden.core.IdGenerator
import org.sarden.core.SystemErrors.DataInconsistencyError
import org.sarden.core.mapping.given
import org.sarden.core.plant.PlantId
import org.sarden.core.sowlog.*
import org.sarden.core.tx.*

private[sowlog] trait SowlogRepo:
  def getLog(): URIO[Tx, Vector[SowlogEntry[PlantId]]]
  def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): URIO[Tx, SowlogEntryId]

class LiveSowlogRepo(idGenerator: IdGenerator) extends SowlogRepo:
  override def getLog(): URIO[Tx, Vector[SowlogEntry[PlantId]]] =
    Tx {
      sql"SELECT id, plant_id, sowing_date, details FROM sowlog"
        .queryTransform[SowlogEntryDTO, SowlogEntry[PlantId]](
          _.intoPartial[SowlogEntry[PlantId]]
            .withFieldRenamed(_.plantId, _.plant)
            .withFieldComputed(
              _.sowingDate,
              _.sowingDate
                .fromJson[LocalDate]
                .getOrElse(
                  throw DataInconsistencyError("Invalid LocalDate value"),
                ),
            )
            .transform,
        )
        .to[Vector]
    }

  override def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): URIO[Tx, SowlogEntryId] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.instant.map(_.getEpochSecond)
      _ <- Tx {
        sql"""INSERT INTO sowlog
             |(id, plant_id, sowing_date, details, created_at)
             |VALUES
             |(${id}, ${plantId}, ${sowingDate}, '{}', ${now})""".stripMargin.update.run
      }
      _ <- ZIO.unit
    yield SowlogEntryId(id)
