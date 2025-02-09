package org.sarden.core

import javax.sql.DataSource

import io.github.gaelrenoux.tranzactio.doobie.*
import org.sqlite.SQLiteDataSource
import zio.*

import org.sarden.core.plant.PlantService
import org.sarden.core.todo.TodoService
import org.sarden.core.weather.WeatherService

def wireLive: URLayer[
  CoreConfig,
  TodoService & WeatherService & PlantService & Migrator,
] =
  val dbLayer = Database.fromDatasource
  val connectionPoolLayer: URLayer[CoreConfig, DataSource] = ZLayer.fromZIO:
    ZIO.serviceWith[CoreConfig] { config =>
      val dataSource = new SQLiteDataSource()
      dataSource.setUrl(config.dbUrl)
      dataSource
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
