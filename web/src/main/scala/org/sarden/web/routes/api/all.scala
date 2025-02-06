package org.sarden.web.routes.api

import org.sarden.web.AppServerEndpoint

val apiRoutes: List[AppServerEndpoint] =
  weatherEndpoints ++ todoEndpoints
