# Conventions:
* Date times are stored as unix epoch seconds in UTC
* Data constraints for newtypes are handled with `iron`. Neotype `validate` MUST NOT be used.
* Domain types are not allowed to derive storage/transport layer specific codecs
  * Domain types should be mapped to DTOs in the storage/transport layer
  * Naming conventions:
    * Database dtos have `DTO` postfix
    * Api dtos have `Request` or `Response` postfix
    * Page dtos have `Form` or `VM` (view model) postfix
* Error handling strategy:
  * Errors are first split into two categories:
    * Invalid requests: 
      It's possible to provide a useful error message to the user.
    * Internal errors: 
      It's not possible to provide a useful error message.
      It includes data inconsistency errors (which indicate programming error or data corruption) 
      and low level system (or driver) errors
  * Every error raised by the system (either directly or indirectly through library calls) should be wrapped in
    one of the above categories.
  * Unrecoverable errors should not be visible in the types. They should be moved to the failure channel with `.orDie` and handled by the global exception handler