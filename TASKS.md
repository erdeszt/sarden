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
* Remove json/doobie instances from domain files
  * Create api dtos
  * Create db dtos
  * Use chimney for mapping
* Setup proper connection pooling
* Setup proper logging
* Setup proper error handling
  * Define domain errors vs system errors
* Setup base test for zio service tests
* Load config with zio-config
  * From env with defaults for non secret values
* Fix .toOption.get calls in codecs
* Setup CI with Werror enabled
* Check if LiveMigrator handles error correctly
* Switch to neotype instead of opaque types(better ergonomics)
  * Maybe not? If codecs are removed from domain it might not be necessary
  * Otherwise
    * Fix newtype schemas
    * Autoderive more
    * Fix unsafe mapping to ulid
* Ulid type in tapir layer
* Use host/user/pass instead of dbUrl for migrator