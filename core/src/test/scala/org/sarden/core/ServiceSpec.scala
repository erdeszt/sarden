package org.sarden.core

import java.time.ZoneId

import org.flywaydb.core.Flyway
import org.scalatest.flatspec.FixtureAnyFlatSpec
import org.scalatest.{BeforeAndAfterEach, Outcome}
import scalikejdbc.ConnectionPool

class ServiceSpec extends FixtureAnyFlatSpec with BeforeAndAfterEach:

  override protected type FixtureParam = CoreServices

  override def withFixture(test: OneArgTest): Outcome =
    super.withFixture(test.toNoArgTest(coreServices))

  def coreConfig: CoreConfig =
    CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:dev.db")

  def coreServices: CoreServices =
    wireLive(coreConfig)

  override def beforeEach(): Unit =
    val flyway = Flyway
      .configure()
      .cleanDisabled(false)
      .dataSource(coreConfig.dbUrl, "", "")
      .load()
    flyway.getConfiguration
    flyway.clean()
    flyway.migrate()

    Class.forName("org.sqlite.JDBC")

    ConnectionPool.singleton(coreConfig.dbUrl, "", "")

    ()
