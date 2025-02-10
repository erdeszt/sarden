package org.sarden.web.routes.pages

import org.sarden.web.AppServerEndpoint

val pageRoutes: List[AppServerEndpoint] = List(
  cssAssetsServerEndpoint,
  jsAssetsServerEndpoint,
  imageAssetsServerEndpoint,
  index,
  plants.listPlants,
  plants.createPlantForm,
  plants.createPlant,
  todo.listTodos,
  todo.deleteTodo,
)
