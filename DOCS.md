# Conventions:
* Date times are stored as unix epoch seconds in UTC
* Data constraints for newtypes are handled with `iron`. Neotype `validate` MUST NOT be used.
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
  * Unrecoverable errors should not be visible in the types(as they should not be handled at the service level).
    They should be moved to the failure channel with `.orDie` and handled by the global exception handler
  * Recoverable errors should only be raised in the service level and they should be propagated in the error channel
    to a global exception handler.