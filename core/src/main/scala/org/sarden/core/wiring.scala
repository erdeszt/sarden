package org.sarden.core

import javax.sql.DataSource

import io.github.gaelrenoux.tranzactio.doobie.*
import org.sqlite.SQLiteDataSource
import zio.*

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

def wireLive: URLayer[
  CoreConfig,
  TodoService & WeatherService & PlantService & Migrator,
] =
  val dbLayer = Database.fromDatasource
  val connectionPoolLayer: URLayer[CoreConfig, DataSource] = ZLayer.fromZIO {
    ZIO.service[CoreConfig].map { config =>
      val dataSource = new SQLiteDataSource()
      dataSource.setUrl(config.dbUrl)
      dataSource
    }
  }

  ZLayer.makeSome[
    CoreConfig,
    TodoService & WeatherService & PlantService & Migrator,
  ](
    connectionPoolLayer,
    dbLayer,
    Migrator.live,
    IdGenerator.live,
    TodoService.live,
    WeatherService.live,
    PlantService.live,
  )
