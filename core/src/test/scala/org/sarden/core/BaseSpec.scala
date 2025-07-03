package org.sarden.core

import java.time.ZoneId
import org.flywaydb.core.Flyway
import org.sarden.{CoreConfig, CoreServices}
import org.sarden.bindings.Migrator
import zio.*
import zio.test.*

abstract class BaseSpec
    extends ZIOSpec[TestEnvironment & CoreServices & Migrator]:

  private val coreConfig = CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:test.db")

  override val bootstrap: ULayer[Environment] =
    ZLayer.make[Environment](
      testEnvironment,
      CoreServices.live,
      ZLayer.succeed(coreConfig),
    )

  def setupDb: Task[Unit] =
    ZIO.attemptBlocking {
      val flyway = Flyway
        .configure()
        .cleanDisabled(false)
        .dataSource(coreConfig.dbUrl, "", "")
        .load()
      flyway.getConfiguration
      flyway.clean()
      flyway.migrate()
    }.unit
