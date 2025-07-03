package org.sarden

import io.github.gaelrenoux.tranzactio.doobie.*
import org.sarden.bindings.{IdGenerator, Migrator, PasswordHasher}
import org.sarden.domain.plant.PlantService
import org.sarden.domain.sowlog.SowlogService
import org.sarden.domain.user.UserService
import org.sqlite.SQLiteDataSource
import zio.*

import javax.sql.DataSource

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
