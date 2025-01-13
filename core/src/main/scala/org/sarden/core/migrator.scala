package org.sarden.core

import org.flywaydb.core.*
import org.sqlite.SQLiteDataSource

trait Migrator:
  def migrate(): Unit

class LiveMigrator(dbUrl: String) extends Migrator:
  override def migrate(): Unit =
    SQLiteDataSource()
    val flyway = Flyway.configure().dataSource(dbUrl, "", "").load()
    // TODO: Check if we need to validate MigrationResult or if it throws anyway
    val _ = flyway.migrate()

    ()
