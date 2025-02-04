package org.sarden.web.endpoints

import sttp.tapir.Schema
import sttp.tapir.ztapir.*

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

def plantEndpoints(service: PlantService): List[ZServerEndpoint[Any, Any]] =
  List(
    viewPlantsEndpoint.zServerLogic[Any] { (_: Unit) =>
      service.searchPlants(SearchPlantFilters(None))
    },
  )
