package org.sarden.core.sowlog

import java.time.LocalDate

import cats.data.NonEmptyList
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.SystemErrors.DataInconsistencyError
import org.sarden.core.plant.*
import org.sarden.core.sowlog.internal.*
import org.sarden.core.tx.*

trait SowlogService:
  def getEntries(): UIO[Vector[SowlogEntry[Plant]]]
  def createEntry(
      plantId: PlantId,
      sowingDate: LocalDate,
      details: SowlogDetails,
  ): UIO[SowlogEntryId]

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
                    _ =>
                      DataInconsistencyError(
                        s"Can't find plant for sow log entry: ${entry.id}",
                      ),
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
