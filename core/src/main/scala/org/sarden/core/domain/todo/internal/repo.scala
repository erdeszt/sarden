package org.sarden.core.domain.todo.internal

import java.time.{Instant, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*

import com.github.f4b6a3.ulid.Ulid
import doobie.implicits.given
import io.github.gaelrenoux.tranzactio.*
import io.github.gaelrenoux.tranzactio.doobie.*
import zio.*

import org.sarden.core.IdGenerator
import org.sarden.core.domain.todo.*

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
  def getActiveTodos(): URIO[Connection, Vector[Todo]]
  def createTodo(todo: CreateTodo): URIO[Connection, Todo]
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): URIO[Connection, Unit]
  def deleteTodo(id: TodoId): URIO[Connection, Unit]

class LiveTodoRepo(idGenerator: IdGenerator) extends TodoRepo:

  override def getActiveTodos(): URIO[Connection, Vector[Todo]] =
    tzio {
      sql"SELECT * FROM todo".query[Todo].to[Vector]
    }.orDie

  override def createTodo(todo: CreateTodo): URIO[Connection, Todo] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.currentDateTime
      _ <- ZIO.attemptBlocking {
//        DB.autoCommit { implicit session =>
//          sql"""INSERT INTO todo
//           (id, name, schedule, notify_before, last_run, created_at)
//           VALUES
//           ( ${id}
//           , ${todo.name.unwrap}
//           , ${upickle.default.write(todo.schedule)}
//           , ${upickle.default.write(todo.notifyBefore)}
//           , NULL
//           , ${now.toInstant.getEpochSecond})""".update.apply()
//        }
        ???
      }.orDie
    yield Todo(
      TodoId(id),
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(
      id: TodoId,
      lastRun: OffsetDateTime,
  ): URIO[Connection, Unit] =
    ZIO.attempt(???).orDie

  override def deleteTodo(id: TodoId): URIO[Connection, Unit] =
    tzio {
      sql"DELET FROM todo WHERE id = ${id}".update.run
    }.orDie.unit
