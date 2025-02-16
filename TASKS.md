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
* User service:
  * Multi user
  * Roles
  * Admin UI

## Tech debt:
* Authentication for web service
* Setup proper error handling
  * Both in core and on the tapir routes
* Use iron to refine domain types
* i18n
* Setup proper connection pooling
  * Switch to postgres
* Load config with zio-config
  * From env with defaults for non secret values
* Setup proper logging
  * Define domain errors vs system errors
* Setup base test for zio service tests
* Fix .toOption.get calls in codecs
* Setup CI with Werror enabled
* Check if LiveMigrator handles error correctly
* Use host/user/pass instead of dbUrl for migrator
* Multiple image format support for assets