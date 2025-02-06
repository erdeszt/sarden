# TASKS:
## Features:
* Plant db:
  * Finish implementation
  * Figure out plant details
  * UI
  * Tests
* SowingLog:
  * Finish implementation
  * UI
  * Tests
* Weather:
  * Handle filters when getting measurements
  * UI for displaying temperatures(plot)
  * Tests
* Todos:
  * Finish notifier loop
  * Tests

## Tech debt:
* Remove htmx
* Remove json/doobie instances from domain files
  * Create api dtos
  * Create db dtos
* Setup proper connection pooling
* Setup proper logging
* Setup proper error handling
  * Define domain errors vs system errors
* Setup base test for zio service tests
* Load config with zio-config
* Fix .toOption.get calls in codecs
* Setup CI with Werror enabled
* Check if LiveMigrator handles error correctly
* Switch to neotype instead of opaque types(better ergonomics)
    * Fix newtype schemas
    * Autoderive more
    * Fix unsafe mapping to ulid
* Load app config from env
* Ulid type in tapir layer
* Use host/user/pass instead of dbUrl for migrator