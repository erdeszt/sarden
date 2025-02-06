package org.sarden.web.routes.pages.todo

import com.github.f4b6a3.ulid.Ulid
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.domain.todo.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

val deleteTodo: AppServerEndpoint = baseEndpoint.post
  .in("todos" / "delete" / path[String]("id"))
  .out(
    statusCode(StatusCode.Found)
      .and(header[String](HeaderNames.Location)),
  )
  .zServerLogic(id =>
    ZIO
      .serviceWithZIO[TodoService](_.deleteTodo(TodoId(Ulid.from(id))))
      .as("/todos"),
  )
