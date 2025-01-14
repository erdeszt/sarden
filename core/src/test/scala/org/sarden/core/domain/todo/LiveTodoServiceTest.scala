package org.sarden.core.domain.todo

import java.time.LocalTime

import scala.concurrent.duration.*

import org.sarden.core.ServiceSpec

class LiveTodoServiceTest extends ServiceSpec:

  "getActiveTodos" should "return all the created todos" in { services =>
    val schedule = Schedule.EverySecondFridayOfTheMonth(LocalTime.of(13, 0))
    val notifyBefore = 24.hours

    val todo = services.todo.createTodo(
      CreateTodo(
        TodoName("test"),
        schedule,
        notifyBefore,
      ),
    )

    val todos = services.todo.getActiveTodos()

    assert(todos.length == 1)
    assert(todos.head.id == todo.id)
    assert(todos.head.name == todo.name)
    assert(todos.head.schedule == schedule)
    assert(todos.head.notifyBefore.equals(notifyBefore))
    assert(todos.head.lastRun.isEmpty)
  }
