package org.sarden.core.domain.todo

import java.time.{LocalTime, ZoneId}

import scala.concurrent.duration.*

import org.flywaydb.core.Flyway
import zio.*
import zio.test.*

import org.sarden.core.CoreConfig

object LiveTodoServiceTest extends ZIOSpecDefault:

  def coreConfig: CoreConfig =
    CoreConfig(ZoneId.of("UTC"), "jdbc:sqlite:test.db")

  def testConfig: ULayer[CoreConfig] = ZLayer.succeed(coreConfig)

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
//
//      Class.forName("org.sqlite.JDBC")
//
//      ConnectionPool.singleton(coreConfig.dbUrl, "", "")
//
      ()
    }

  def spec =
    suite("Live TodoService Test")(
      test(".getActiveTodos should return all the created todos") {
        val schedule =
          TodoSchedule.EverySecondFridayOfTheMonth(LocalTime.of(13, 0))
        val notifyBefore: FiniteDuration = 24.hours

        for
          todoService <- ZIO.service[TodoService]
          todo <- todoService.createTodo(
            CreateTodo(
              TodoName("test"),
              schedule,
              notifyBefore,
            ),
          )

          todos <- todoService.getActiveTodos()
        yield assertTrue(
          todos.length == 1,
          todos.head.id == todo.id,
          todos.head.name == todo.name,
          todos.head.schedule == schedule,
          todos.head.notifyBefore.equals(notifyBefore),
          todos.head.lastRun.isEmpty,
        )
      },
    ).provide(testConfig, org.sarden.core.wireLive) @@ TestAspect
      .before(
        setupDb,
      )
