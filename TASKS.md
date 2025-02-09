# TASKS:
## Features:
* Plant db:
  * Finish implementation
    * Add/Delete variety
    * List varieties on plant site
  * Figure out plant details
  * UI
  * Tests
* SowingLog:
  * Finish implementation
    * Sow date, plant, variety, pot/direct, indoor/outdoor/greenhouse, amount(per cell, cell) 
    * List plantings
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
* Use iron to refine domain types
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
* Use host/user/pass instead of dbUrl for migrator