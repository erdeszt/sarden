package org.sarden.web.routes.pages.todo

import com.github.f4b6a3.ulid.Ulid
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.ztapir.*
import zio.*

import org.sarden.core.todo.*
import org.sarden.web.AppServerEndpoint
import org.sarden.web.routes.pages.*

val deleteTodo: AppServerEndpoint = baseEndpoint.post
  .in("todos" / "delete" / path[String]("id"))
  .out(
    statusCode(StatusCode.Found)
      .and(header[String](HeaderNames.Location)),
  )
  .zServerLogic { id =>
    ZIO.serviceWithZIO[TodoService]:
      // TODO: Safe id conversion
      _.deleteTodo(TodoId(Ulid.from(id))).as("/todos")
  }
