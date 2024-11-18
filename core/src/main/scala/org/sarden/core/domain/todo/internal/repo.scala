package org.sarden.core.domain.todo.internal

import java.time.{Instant, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*

import com.github.f4b6a3.ulid.Ulid
import scalikejdbc.*

import org.sarden.core.domain.todo.*
import org.sarden.core.{Clock, IdGenerator}

// Info on jvm date types: https://stackoverflow.com/questions/32437550

private[internal] case class TodoDTO(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: String,
    lastRun: Option[Long],
    createdAt: Long,
)

// TODO: Transaction in types
// TODO: Better handling of connections
// TODO: Replace `relate` with `scalikeJdbc`,
private[todo] trait TodoRepo:
  def getActiveTodos(): List[Todo]
  def createTodo(todo: CreateTodo): Todo
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit

class LiveTodoRepo(clock: Clock, idGenerator: IdGenerator)(using DBSession)
    extends TodoRepo:

  override def getActiveTodos(): List[Todo] =
    sql"SELECT * FROM todo"
      .map { row =>
        Todo(
          TodoId(Ulid.from(row.string("id"))),
          TodoName(row.string("name")),
          upickle.default.read[Schedule](row.string("schedule")),
          upickle.default.read[FiniteDuration](row.string("notify_before")),
          row.longOpt("last_run").map { lastRun =>
            OffsetDateTime.from(Instant.ofEpochSecond(lastRun))
          },
        )
      }
      .list
      .apply()

  override def createTodo(todo: CreateTodo): Todo =
    val id = idGenerator.next()
    val now = clock.currentDateTime()

    sql"""INSERT INTO todo
           (id, name, schedule, notify_before, last_run, created_at)
           VALUES
           ( ${id.toString}
           , ${todo.name.unwrap}
           , ${upickle.default.write(todo.schedule)}
           , ${upickle.default.write(todo.notifyBefore)}
           , NULL
           , ${now.toInstant.getEpochSecond})""".update.apply()

    Todo(
      TodoId(id),
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit =
    ???
