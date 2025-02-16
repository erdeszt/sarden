package org.sarden.core

import java.time.LocalTime

import zio.json.*
import zio.test.*

object MappingsTest extends ZIOSpecDefault:

  def spec =
    suite("Mappings")(
      suite("Java Time JSON mapping references")(
        test("LocalTime")(
          assertTrue(LocalTime.of(15, 0).toJson == "\"15:00:00\""),
        ),
      ),
    )
