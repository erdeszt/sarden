package org.sarden.core.todo.internal

import java.time.OffsetDateTime

import com.github.f4b6a3.ulid.Ulid
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.todo.*
import org.sarden.core.todo.given
import org.sarden.core.tx.*

// Info on jvm date types: https://stackoverflow.com/questions/32437550

private[internal] case class TodoDTO(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: String,
    lastRun: Option[Long],
    createdAt: Long,
)

private[todo] trait TodoRepo:
  def getActiveTodos(): URIO[Tx, Vector[Todo]]
  def createTodo(todo: CreateTodo): URIO[Tx, Todo]
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): URIO[Tx, Unit]
  def deleteTodo(id: TodoId): URIO[Tx, Unit]

class LiveTodoRepo(idGenerator: IdGenerator) extends TodoRepo:

  override def getActiveTodos(): URIO[Tx, Vector[Todo]] =
    Tx {
      sql"SELECT id, name, schedule, notify_before, last_run FROM todo"
        .query[Todo]
        .to[Vector]
    }

  override def createTodo(todo: CreateTodo): URIO[Tx, Todo] =
    for
      id <- idGenerator.next().map(TodoId(_))
      now <- zio.Clock.currentDateTime
      _ <- Tx {
        sql"""INSERT INTO todo
             |(id, name, schedule, notify_before, last_run, created_at)
             |VALUES
             |( ${id}
             |, ${todo.name}
             |, ${todo.schedule}
             |, ${todo.notifyBefore}
             |, NULL
             |, ${now})""".stripMargin.update.run
      }
    yield Todo(
      id,
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(
      id: TodoId,
      lastRun: OffsetDateTime,
  ): URIO[Tx, Unit] =
    ZIO.attempt(???).orDie

  override def deleteTodo(id: TodoId): URIO[Tx, Unit] =
    Tx {
      sql"DELETE FROM todo WHERE id = ${id}".update.run
    }.unit
