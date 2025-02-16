package org.sarden.core.plant.internal

import doobie.Read
import zio.json.JsonCodec

private[internal] case class PlantDTO(
    id: String,
    name: String,
) derives Read

private[internal] case class CreatePlantDTO(
    name: String,
) derives JsonCodec
