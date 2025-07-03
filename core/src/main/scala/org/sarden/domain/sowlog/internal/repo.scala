package org.sarden.domain.sowlog.internal

import java.time.LocalDate

import io.scalaland.chimney.dsl.*
import zio.*

import org.sarden.bindings.*
import org.sarden.bindings.tx.*
import org.sarden.domain.plant.PlantId
import org.sarden.domain.sowlog.*
import org.sarden.domain.sowlog.{SowlogDetails, SowlogEntry, SowlogEntryId}
import mapping.given

private[sowlog] trait SowlogRepo:
  def getLog(): URIO[Tx, Vector[SowlogEntry[PlantId]]]
  def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): URIO[Tx, SowlogEntryId]
  def deleteEntry(id: SowlogEntryId): URIO[Tx, Unit]

class LiveSowlogRepo(idGenerator: IdGenerator) extends SowlogRepo:
  override def getLog(): URIO[Tx, Vector[SowlogEntry[PlantId]]] =
    Tx {
      sql"SELECT id, plant_id, sowing_date, details FROM sowlog"
        .queryTransformPartial[SowlogEntryDTO](
          _.intoPartial[SowlogEntry[PlantId]]
            .withFieldRenamed(_.plantId, _.plant)
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

  override def deleteEntry(id: SowlogEntryId): URIO[Tx, Unit] =
    Tx {
      sql"DELETE FROM sowlog WHERE id = ${id}".update.run
    }.unit
