package org.sarden.web.routes.pages.plants

import sttp.tapir.Schema

private[pages] case class PlantVM(
    id: String,
    name: String,
) derives Schema

private[pages] case class CompanionVM(
    id: String,
    companionPlant: PlantVM,
    targetPlant: PlantVM,
    benefits: Set[String],
) derives Schema
