package org.sarden.core

import javax.sql.DataSource

import io.github.gaelrenoux.tranzactio.doobie.*
import org.sqlite.SQLiteDataSource
import zio.*

import org.sarden.core.plant.PlantService
import org.sarden.core.sowlog.SowlogService
import org.sarden.core.user.UserService

type CoreServices = PlantService & UserService & SowlogService

object CoreServices:
  def live: URLayer[
    CoreConfig,
    CoreServices & Migrator,
  ] =
    val dbLayer = Database.fromDatasource
    val connectionPoolLayer: URLayer[CoreConfig, DataSource] = ZLayer.fromZIO:
      ZIO.serviceWith[CoreConfig] { config =>
        val dataSource = new SQLiteDataSource()
        dataSource.setUrl(config.dbUrl)
        dataSource
      }

    ZLayer.makeSome[
      // TODO: Consider loading the config here?
      CoreConfig,
      CoreServices & Migrator,
    ](
      connectionPoolLayer,
      dbLayer,
      Migrator.live,
      IdGenerator.live,
      PasswordHasher.live,
      PlantService.live,
      UserService.live,
      SowlogService.live,
    )
