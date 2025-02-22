package org.sarden.core.plant

import cats.data.NonEmptyList
import com.github.f4b6a3.ulid.Ulid
import zio.*
import zio.test.*

import org.sarden.core.*
import org.sarden.core.mapping.given

object LivePlantServiceTest extends BaseSpec:

  given canEqualUnion[A1, AS](using CanEqual[A1, A1]): CanEqual[A1 | AS, A1] =
    CanEqual.derived

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
          plant.id == plantId,
          plant.name == plantName,
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
          getByIdResult <- plantService
            .getPlant(plantId)
            .map(Some(_))
            .catchSome { case _: MissingPlantError => ZIO.none }
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
      test("Added varieties can be listed") {
        for
          plantService <- ZIO.service[PlantService]
          carrotId <- plantService
            .createPlant(PlantName("carrot"), PlantDetails())
          boleroName = VarietyName("bolero")
          boleroId <- plantService.createVariety(carrotId, boleroName)
          carrotVarieties <- plantService.getVarietiesOfPlant(carrotId)
        yield assertTrue(
          carrotVarieties.length == 1,
          carrotVarieties.head.id == boleroId,
          carrotVarieties.head.name == boleroName,
          carrotVarieties.head.plant == carrotId,
        )
      },
      test("Adding variety to non existing plant causes an error") {
        for
          plantService <- ZIO.service[PlantService]
          plantId = PlantId(Ulid.fast())
          result <- plantService
            .createVariety(plantId, VarietyName("whatever"))
            .either
        yield assertTrue(
          result == Left(MissingPlantError(plantId)),
        )
      },
      test("Add companions can be listed") {
        for
          plantService <- ZIO.service[PlantService]
          carrotId <- plantService
            .createPlant(PlantName("carrot"), PlantDetails())
          onionId <- plantService
            .createPlant(PlantName("onion"), PlantDetails())
          companionId <- plantService.createCompanion(
            onionId,
            carrotId,
            Set(CompanionBenefit.DetersPests),
          )
          carrotCompanions <- plantService.getCompanionsOfPlant(carrotId)
        yield assertTrue(
          carrotCompanions.length == 1,
          carrotCompanions.head.id == companionId,
          carrotCompanions.head.companionPlant.id == onionId,
          carrotCompanions.head.targetPlant.id == carrotId,
          carrotCompanions.head.benefits == Set(CompanionBenefit.DetersPests),
        )
      },
      test("Adding a companion to non existing plant causes an error") {
        for
          plantService <- ZIO.service[PlantService]
          onionId <- plantService
            .createPlant(PlantName("onion"), PlantDetails())
          plantId = PlantId(Ulid.fast())
          result <- plantService
            .createCompanion(onionId, plantId, Set.empty)
            .either
        yield assertTrue(
          result == Left(MissingPlantError(plantId)),
        )
      },
      test("Adding a non existing companion to a plant causes an error") {
        for
          plantService <- ZIO.service[PlantService]
          carrotId <- plantService
            .createPlant(PlantName("carrot"), PlantDetails())
          plantId = PlantId(Ulid.fast())
          result <- plantService
            .createCompanion(plantId, carrotId, Set.empty)
            .either
        yield assertTrue(
          result == Left(MissingPlantError(plantId)),
        )
      },
      test("Adding a plant as a companion to itself should be forbidden") {
        for
          plantService <- ZIO.service[PlantService]
          carrotId <- plantService
            .createPlant(PlantName("carrot"), PlantDetails())
          result <- plantService
            .createCompanion(carrotId, carrotId, Set.empty)
            .either
        yield assertTrue(result == Left(SelfCompanionError(carrotId)))
      },
      test("Adding a companion twice should be forbidden") {
        for
          plantService <- ZIO.service[PlantService]
          carrotId <- plantService.createPlant(
            PlantName("carrot"),
            PlantDetails(),
          )
          onionId <- plantService.createPlant(
            PlantName("onion"),
            PlantDetails(),
          )
          companionId <- plantService.createCompanion(
            carrotId,
            onionId,
            Set.empty,
          )
          result <- plantService
            .createCompanion(carrotId, onionId, Set.empty)
            .either
        yield assertTrue(
          result == Left(CompanionAlreadyExistsError(companionId)),
        )
      },
    ) @@ TestAspect.before(setupDb) @@ TestAspect.sequential
