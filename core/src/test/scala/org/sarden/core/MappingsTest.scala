package org.sarden.core

import java.time.{LocalDate, LocalTime}

import zio.json.*
import zio.test.*

object MappingsTest extends ZIOSpecDefault:

  def spec =
    suite("Mappings")(
      suite("Java Time JSON mapping references")(
        test("LocalTime")(
          assertTrue(LocalTime.of(15, 0).toJson == "\"15:00:00\""),
        ),
        test("LocalDate")(
          assertTrue(LocalDate.of(2025, 2, 15).toJson == "\"2025-02-15\""),
        ),
      ),
    )
