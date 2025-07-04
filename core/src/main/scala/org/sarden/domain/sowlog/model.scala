package org.sarden.domain.sowlog

import java.time.LocalDate

import org.sarden.bindings.ulid.UlidNewtype

type SowlogEntryId = SowlogEntryId.Type
object SowlogEntryId extends UlidNewtype

case class SowlogEntry[PlantType](
    id: SowlogEntryId,
    plant: PlantType,
    sowingDate: LocalDate,
    details: SowlogDetails,
)

case class SowlogDetails()
