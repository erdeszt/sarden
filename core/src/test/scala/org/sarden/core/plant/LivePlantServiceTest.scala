package org.sarden.core.plant

import cats.data.NonEmptyList
import zio.*
import zio.test.*

import org.sarden.core.*
import org.sarden.core.mapping.given

object LivePlantServiceTest extends BaseSpec:

  override def spec =
    suite("Live PlantService Test")(
      test("Created plants should be returned when searching with no filters") {
        val plantName = PlantName("carrot")
        for
          plantService <- ZIO.service[PlantService]
          plantId <- plantService
            .createPlant(plantName, PlantDetails())
          plants <- plantService.searchPlants(SearchPlantFilters.empty)
        yield assertTrue(
          plants.length == 1,
          plants.head.id == plantId,
          plants.head.name == plantName,
        )
      },
      test("Created plant should be returned when searching by id") {
        val plantName = PlantName("carrot")
        for
          plantService <- ZIO.service[PlantService]
          plantId <- plantService
            .createPlant(plantName, PlantDetails())
          plant <- plantService.getPlant(plantId)
        yield assertTrue(
          plant.nonEmpty,
          plant.get.id == plantId,
          plant.get.name == plantName,
        )
      },
      test("Created plant should be returned when searching by ids") {
        val plantName = PlantName("carrot")
        for
          plantService <- ZIO.service[PlantService]
          plantId <- plantService
            .createPlant(plantName, PlantDetails())
          plant <- plantService.getPlantsByIds(NonEmptyList.of(plantId))
        yield assertTrue(
          plant.size == 1,
          plant(plantId).id == plantId,
          plant(plantId).name == plantName,
        )
      },
      test(
        "Deleted plants should not be returned by any of the lookup methods",
      ) {
        for
          plantService <- ZIO.service[PlantService]
          plantId <- plantService.createPlant(
            PlantName("carrot"),
            PlantDetails(),
          )
          _ <- plantService.deletePlant(plantId)
          searchResult <- plantService.searchPlants(SearchPlantFilters.empty)
          getByIdResult <- plantService.getPlant(plantId)
          getByIdsResult <- plantService.getPlantsByIds(
            NonEmptyList.of(plantId),
          )
        yield assertTrue(
          searchResult.isEmpty,
          getByIdResult.isEmpty,
          getByIdsResult.isEmpty,
        )
      },
      test("Load preset data should load some plants") {
        for
          plantService <- ZIO.service[PlantService]
          _ <- plantService.loadPresetData
          plants <- plantService.searchPlants(SearchPlantFilters.empty)
        yield assertTrue(plants.nonEmpty)
      },
    ) @@ TestAspect.before(setupDb) @@ TestAspect.sequential
