package org.sarden.core

trait Migrator:
  def migrate(): Unit

class LiveMigrator extends Migrator:
  override def migrate(): Unit =
    ???
