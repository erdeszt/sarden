package org.sarden.web.views

import scalatags.Text.TypedTag
import scalatags.Text.all.*

import org.sarden.core.domain.plant.Plant

def viewPlants(plants: Vector[Plant]): TypedTag[String] =
  layout(div("Plants coming soon..."))
