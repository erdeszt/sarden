package org.sarden.web.endpoints
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.Schema
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.domain.plant.*
import org.sarden.core.domain.plant.Plant
import org.sarden.web.*

given Schema[PlantId] = Schema.string
given Schema[PlantName] = Schema.string
given Schema[PlantDetails] = Schema.derived
given Schema[Plant] = Schema.derived

val viewPlantsEndpoint = endpoint.get
  .in("plants")
  .out(htmlView[Vector[Plant]](views.viewPlants))

def plantEndpoints: List[AppServerEndpoint] =
  List(
    viewPlantsEndpoint.zServerLogic { (_: Unit) =>
      ZIO.serviceWithZIO[PlantService](_.searchPlants(SearchPlantFilters(None)))
    },
  )
