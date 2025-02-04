package org.sarden.core.domain.todo.internal

import java.time.{Instant, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*

import com.github.f4b6a3.ulid.Ulid
import scalikejdbc.*
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
  def getActiveTodos(): UIO[List[Todo]]
  def createTodo(todo: CreateTodo): UIO[Todo]
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): UIO[Unit]
  def deleteTodo(id: TodoId): UIO[Unit]

class LiveTodoRepo(idGenerator: IdGenerator) extends TodoRepo:

  override def getActiveTodos(): UIO[List[Todo]] =
    ZIO.attemptBlocking {
      DB.autoCommit { implicit session =>
        sql"SELECT * FROM todo"
          .map { row =>
            Todo(
              TodoId(Ulid.from(row.string("id"))),
              TodoName(row.string("name")),
              upickle.default.read[TodoSchedule](row.string("schedule")),
              upickle.default.read[FiniteDuration](row.string("notify_before")),
              row.longOpt("last_run").map { lastRun =>
                OffsetDateTime.from(Instant.ofEpochSecond(lastRun))
              },
            )
          }
          .list
          .apply()
      }
    }.orDie

  override def createTodo(todo: CreateTodo): UIO[Todo] =
    for
      id <- idGenerator.next()
      now <- zio.Clock.currentDateTime
      _ <- ZIO.attemptBlocking {
        DB.autoCommit { implicit session =>
          sql"""INSERT INTO todo
           (id, name, schedule, notify_before, last_run, created_at)
           VALUES
           ( ${id}
           , ${todo.name.unwrap}
           , ${upickle.default.write(todo.schedule)}
           , ${upickle.default.write(todo.notifyBefore)}
           , NULL
           , ${now.toInstant.getEpochSecond})""".update.apply()
        }
      }.orDie
    yield Todo(
      TodoId(id),
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(id: TodoId, lastRun: OffsetDateTime): UIO[Unit] =
    ZIO.attempt(???).orDie

  override def deleteTodo(id: TodoId): UIO[Unit] =
    ZIO
      .attemptBlocking {
        DB.autoCommit { implicit session =>
          sql"DELETE FROM todo WHERE id = ${id.unwrap}".update.apply()
        }
      }
      .orDie
      .unit
