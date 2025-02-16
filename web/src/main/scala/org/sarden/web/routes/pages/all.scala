package org.sarden.web.routes.pages

import org.sarden.web.*

def pageRoutes(using siteAuthConfig: SiteAuthConfig): List[AppServerEndpoint] =
  List(
    cssAssetsServerEndpoint,
    jsAssetsServerEndpoint,
    imageAssetsServerEndpoint,
    index,
    plants.listPlants,
    plants.createPlantForm,
    plants.createPlant,
    todo.listTodos,
    todo.deleteTodo,
    sowlog.showSowlog,
    sowlog.createSowlogEntryForm,
    sowlog.createSowlogEntry,
  )
