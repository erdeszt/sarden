# TASKS:
## Features:
* Weather:
  * Handle filters when getting measurements
  * UI for displaying temperatures(plot)
  * Tests
* Plant db:
  * Finish implementation
  * Figure out plant details
  * UI
  * Tests
* Todos:
  * Finish notifier loop
  * Tests

## Tech debt:
* ZIO + Doobie(or zio sql)
  * Setup proper connection pooling
  * Setup proper logging
  * Setup proper error handling
    * Define domain errors vs system errors
  * Setup base test for zio service tests
  * Load config with zio-config
  * zio-json
  * zio-tapir
  * ZLayer for wiring
* Setup CI with Werror enabled
* Check if LiveMigrator handles error correctly
* Transactions in types
* Better handling of connections/transactions
* Switch to zio-newtypes instead of opaque types(better ergonomics)
    * Fix newtype schemas
    * Autoderive more
* Load app config from env
* Ulid type in tapir layer
* Use host/user/pass instead of dbUrl for migrator