package org.sarden.bindings

import scala.sys.process.*

import org.flywaydb.core.*
import org.sqlite.SQLiteDataSource
import zio.*

import org.sarden.CoreConfig

trait Migrator:
  def migrate(): UIO[Unit]
  def backup(): UIO[Unit]

object Migrator:
  val live: URLayer[CoreConfig, Migrator] =
    ZLayer.fromFunction(LiveMigrator.apply)

class LiveMigrator(config: CoreConfig) extends Migrator:
  override def migrate(): UIO[Unit] =
    ZIO
      .attemptBlocking {
        SQLiteDataSource()
        val flyway = Flyway.configure().dataSource(config.dbUrl, "", "").load()
        // TODO: Check if we need to validate MigrationResult or if it throws anyway
        val _ = flyway.migrate()
      }
      .orDie
      .unit

  override def backup(): UIO[Unit] =
    ZIO
      .attemptBlocking {
        "sqlite3 dev.db \".backup 'dev.backup'\"".!
      }
      .orDie
      .unit
