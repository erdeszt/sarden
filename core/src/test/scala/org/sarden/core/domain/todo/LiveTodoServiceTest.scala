package org.sarden.core.domain.todo

import java.time.{LocalTime, ZoneId}

import scala.concurrent.duration.*

import org.flywaydb.core.Flyway
import scalikejdbc.ConnectionPool
import zio.*
import zio.test.*

import org.sarden.core.{CoreConfig, CoreServices}

object LiveTodoServiceTest extends ZIOSpecDefault:

  def coreConfig: CoreConfig =
    CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:dev.db")

  def coreServices: CoreServices =
    org.sarden.core.wireLive(coreConfig)

  def setupDb: Task[Unit] =
    ZIO.attemptBlocking {
      val flyway = Flyway
        .configure()
        .cleanDisabled(false)
        .dataSource(coreConfig.dbUrl, "", "")
        .load()
      flyway.getConfiguration
      flyway.clean()
      flyway.migrate()

      Class.forName("org.sqlite.JDBC")

      ConnectionPool.singleton(coreConfig.dbUrl, "", "")

      ()
    }

  lazy val services = coreServices

  def spec =
    suite("Live TodoService Test")(
      test(".getActiveTodos should return all the created todos") {
        val schedule =
          TodoSchedule.EverySecondFridayOfTheMonth(LocalTime.of(13, 0))
        val notifyBefore: FiniteDuration = 24.hours

        for
          todo <- services.todo.createTodo(
            CreateTodo(
              TodoName("test"),
              schedule,
              notifyBefore,
            ),
          )

          todos <- services.todo.getActiveTodos()
        yield assertTrue(
          todos.length == 1,
          todos.head.id == todo.id,
          todos.head.name == todo.name,
          todos.head.schedule == schedule,
          todos.head.notifyBefore.equals(notifyBefore),
          todos.head.lastRun.isEmpty,
        )
      },
    ) @@ TestAspect.before(setupDb)
