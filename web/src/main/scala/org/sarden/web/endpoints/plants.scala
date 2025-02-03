package org.sarden.web.endpoints

import sttp.shared.Identity
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint

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

def plantEndpoitns(service: PlantService): List[ServerEndpoint[Any, Identity]] =
  List(
    viewPlantsEndpoint.handleSuccess { (_: Unit) =>
      service.searchPlants(SearchPlantFilters(None))
    },
  )
