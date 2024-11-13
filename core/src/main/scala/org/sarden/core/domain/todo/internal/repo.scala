package org.sarden.core.domain.todo.internal

import java.time.{Instant, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.github.f4b6a3.ulid.Ulid
import org.sql2o.Sql2o

import org.sarden.core.domain.todo.*
import org.sarden.core.{Clock, IdGenerator}

private[internal] case class TodoDTO(
    id: String,
    name: String,
    schedule: String,
    notifyBefore: String,
    lastRun: Option[Long],
    createdAt: Long,
)

// TODO: Transaction in types
private[todo] trait TodoRepo:
  def getActiveTodos(): Vector[Todo]
  def createTodo(todo: CreateTodo): Todo
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit

class LiveTodoRepo(sql2o: Sql2o, clock: Clock, idGenerator: IdGenerator)
    extends TodoRepo:
  override def getActiveTodos(): Vector[Todo] =
    Using(sql2o.open())(
      _.createQuery("SELECT * FROM todo").executeAndFetch(classOf[TodoDTO]),
    ).get.asScala.map { dto =>
      Todo(
        TodoId(Ulid.from(dto.id)),
        TodoName(dto.name),
        upickle.default.read[Schedule](dto.schedule),
        upickle.default.read[FiniteDuration](dto.notifyBefore),
        dto.lastRun.map(lastRun =>
          OffsetDateTime.from(Instant.ofEpochSecond(lastRun)),
        ),
      )
    }.toVector

  override def createTodo(todo: CreateTodo): Todo =
    val id = idGenerator.next()
    val now = clock.currentDateTime()

    Using(sql2o.open())(
      _.createQuery(
        """INSERT INTO todo
        |(id, name, schedule, notify_before, last_run, created_at)
        |VALUES
        |(:id, :name, :schedule, :notify_before, NULL, :created_at)""".stripMargin,
      ).addParameter("id", id.toString)
        .addParameter("name", todo.name)
        .addParameter("schedule", upickle.default.write(todo.schedule))
        .addParameter("notify_before", upickle.default.write(todo.notifyBefore))
        .addParameter("created_at", now.toInstant.getEpochSecond)
        .executeUpdate(),
    ).get

    Todo(
      TodoId(id),
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit =
    ???
