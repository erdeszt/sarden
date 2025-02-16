package org.sarden.core.todo

import java.time.LocalTime

import scala.concurrent.duration.*

import zio.*
import zio.test.*

import org.sarden.core.*

object LiveTodoServiceTest extends BaseSpec:

  def spec =
    suite("Live TodoService Test")(
      test("Getting todos should return all the created todos") {
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
      test("Deleted todos should not be returned") {
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
          _ <- todoService.deleteTodo(todo.id)
          todos <- todoService.getActiveTodos()
        yield assertTrue(todos.isEmpty)
      },
    ) @@ TestAspect.before(setupDb) @@ TestAspect.sequential
