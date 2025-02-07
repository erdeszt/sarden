package org.sarden.core.plant.internal

import doobie.Read

private[internal] case class PlantDTO(
    id: String,
    name: String,
) derives Read
