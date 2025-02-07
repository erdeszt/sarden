package org.sarden.web.routes.schemas

import sttp.tapir.Schema
import org.sarden.core.plant.{Plant, PlantDetails, PlantId, PlantName}

object plants:

  given Schema[PlantId] = Schema.string
  given Schema[PlantName] = Schema.string
  given Schema[PlantDetails] = Schema.derived
  given Schema[Plant] = Schema.derived
