package org.sarden.domain.sowlog

import java.time.LocalDate

import cats.data.NonEmptyList
import zio.*

import org.sarden.InternalError
import org.sarden.bindings.*
import org.sarden.bindings.tx.*
import org.sarden.domain.plant.*
import org.sarden.domain.sowlog.internal.*

case class PlantNotFoundForSowlogError(
    plantId: PlantId,
    sowlogEntryId: SowlogEntryId,
) extends InternalError(
      s"Plant(${plantId}) not found for sowlog: ${sowlogEntryId}",
    )

trait SowlogService:
  def getEntries(): UIO[Vector[SowlogEntry[Plant]]]
  def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): UIO[SowlogEntryId]
  def deleteEntry(id: SowlogEntryId): UIO[Unit]

object SowlogService:
  val live: URLayer[Tx.Runner & PlantService & IdGenerator, SowlogService] =
    ZLayer.fromZIO:
      for
        tx <- ZIO.service[Tx.Runner]
        plants <- ZIO.service[PlantService]
        idGenerator <- ZIO.service[IdGenerator]
      yield LiveSowlogService(LiveSowlogRepo(idGenerator), tx, plants)

class LiveSowlogService(
    repo: SowlogRepo,
    tx: Tx.Runner,
    plantService: PlantService,
) extends SowlogService:

  override def getEntries(): UIO[Vector[SowlogEntry[Plant]]] =
    for
      entries <- tx.runOrDie(repo.getLog())
      enrichedEntries <- NonEmptyList.fromFoldable(entries) match
        case None => ZIO.succeed(Vector.empty[SowlogEntry[Plant]])
        case Some(nonEmptyEntries) =>
          plantService
            .getPlantsByIds(nonEmptyEntries.map(_.plant))
            .flatMap: plants =>
              ZIO.foreach(entries): entry =>
                ZIO
                  .fromOption(plants.get(entry.plant))
                  .mapBoth(
                    _ => PlantNotFoundForSowlogError(entry.plant, entry.id),
                    plant => entry.copy[Plant](plant = plant),
                  )
                  .orDie
    yield enrichedEntries

  override def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): UIO[SowlogEntryId] =
    tx.runOrDie(repo.createEntry(plantId, sowingDate, details))

  override def deleteEntry(id: SowlogEntryId): UIO[Unit] =
    tx.runOrDie(repo.deleteEntry(id))
