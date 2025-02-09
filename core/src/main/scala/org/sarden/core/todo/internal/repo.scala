package org.sarden.core.todo.internal

import io.scalaland.chimney.dsl.*
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.mapping.given
import org.sarden.core.time.*
import org.sarden.core.todo.*
import org.sarden.core.tx.*

// Info on jvm date types: https://stackoverflow.com/questions/32437550

private[todo] trait TodoRepo:
  def getActiveTodos(): URIO[Tx, Vector[Todo]]
  def createTodo(todo: CreateTodo): URIO[Tx, Todo]
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): URIO[Tx, Unit]
  def deleteTodo(id: TodoId): URIO[Tx, Unit]

class LiveTodoRepo(idGenerator: IdGenerator) extends TodoRepo:

  override def getActiveTodos(): URIO[Tx, Vector[Todo]] =
    Tx:
      sql"SELECT * FROM todo"
        .queryTransform[TodoDTO, Todo](
          _.intoPartial[Todo].transform,
        )
        .to[Vector]

  override def createTodo(todo: CreateTodo): URIO[Tx, Todo] =
    for
      id <- idGenerator.next().map(TodoId(_))
      now <- zio.Clock.currentDateTime
      _ <- Tx:
        sql"""INSERT INTO todo
             |(id, name, schedule, notify_before, last_run, created_at)
             |VALUES
             |( ${id}
             |, ${todo.name}
             |, ${todo.schedule.transformInto[String]}
             |, ${todo.notifyBefore}
             |, NULL
             |, ${now})""".stripMargin.update.run
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
