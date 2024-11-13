package org.sarden.core.domain.todo.internal

import java.sql.DriverManager
import java.time.{Instant, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.github.f4b6a3.ulid.Ulid
import com.lucidchart.relate.*

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
// TODO: Better handling of connections
// TODO: Replace `relate` with `scalikeJdbc`
private[todo] trait TodoRepo:
  def getActiveTodos(): List[Todo]
  def createTodo(todo: CreateTodo): Todo
  def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit

class LiveTodoRepo(dbUrl: String, clock: Clock, idGenerator: IdGenerator)
    extends TodoRepo:
  given RowParser[Todo] = new RowParser[Todo] {
    def parse(row: SqlRow): Todo = {
      Todo(
        TodoId(Ulid.from(row.string("id"))),
        TodoName(row.string("name")),
        upickle.default.read[Schedule](row.string("schedule")),
        upickle.default.read[FiniteDuration](row.string("notify_before")),
        row.longOption("last_run").map { lastRun =>
          OffsetDateTime.from(Instant.ofEpochSecond(lastRun))
        },
      )
    }
  }
  override def getActiveTodos(): List[Todo] =
    Using(DriverManager.getConnection(dbUrl)) { implicit connection =>
      sql"SELECT * FROM todo".asList[Todo]()
    }.get

  override def createTodo(todo: CreateTodo): Todo =
    val id = idGenerator.next()
    val now = clock.currentDateTime()

    Using(DriverManager.getConnection(dbUrl)) { implicit connection =>
      sql"""INSERT INTO todo
           (id, name, schedule, notify_before, last_run, created_at)
           VALUES
           ( ${id.toString}
           , ${todo.name.unwrap}
           , ${upickle.default.write(todo.schedule)}
           , ${upickle.default.write(todo.notifyBefore)}
           , NULL
           , ${now.toInstant.getEpochSecond})""".execute()
    }.get

    Todo(
      TodoId(id),
      todo.name,
      todo.schedule,
      todo.notifyBefore,
      None,
    )

  override def updateLastRun(id: TodoId, lastRun: OffsetDateTime): Unit =
    ???
