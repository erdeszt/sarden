package org.sarden.web.routes.pages

import org.sarden.web.AppServerEndpoint

val pageRoutes: List[AppServerEndpoint] = List(
  cssAssetsServerEndpoint,
  jsAssetsServerEndpoint,
  plants.listPlants,
  todo.listTodos,
  todo.deleteTodo,
)
