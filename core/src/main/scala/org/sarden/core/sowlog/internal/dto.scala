package org.sarden.core.sowlog.internal

import doobie.Read

case class SowlogEntryDTO(
    id: String,
    plantId: String,
    sowingDate: String,
    details: String,
) derives Read
