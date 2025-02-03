package org.sarden.core

import org.sarden.core.domain.plant.internal.LivePlantRepo
import org.sarden.core.domain.plant.{LivePlantService, PlantService}
import org.sarden.core.domain.todo.internal.LiveTodoRepo
import org.sarden.core.domain.todo.{LiveTodoService, TodoService}
import org.sarden.core.domain.weather.internal.LiveWeatherRepo
import org.sarden.core.domain.weather.{LiveWeatherService, WeatherService}

case class CoreServices(
    todo: TodoService,
    weather: WeatherService,
    plant: PlantService,
    migrator: Migrator,
)

def wireLive(config: CoreConfig): CoreServices =
  val clock = LiveClock(config.zoneId)
  val idGenerator = LiveIdGenerator()
  val todoRepo = LiveTodoRepo(clock, idGenerator)
  val todoService = LiveTodoService(todoRepo)
  val weatherRepo = LiveWeatherRepo()
  val weatherService = LiveWeatherService(weatherRepo)
  val plantRepo = LivePlantRepo()
  val plantService = LivePlantService(plantRepo)
  val migrator = LiveMigrator(config.dbUrl)

  CoreServices(
    todoService,
    weatherService,
    plantService,
    migrator,
  )
