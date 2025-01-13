package org.sarden.core

import java.time.Instant

import upickle.default.ReadWriter

object json:

  object javatime:
    given ReadWriter[Instant] =
      upickle.default
        .readwriter[Long]
        .bimap(
          _.getEpochSecond,
          Instant.ofEpochSecond,
        )
