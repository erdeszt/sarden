package org.sarden.core.domain.todo

import java.time.{DayOfWeek, LocalTime, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration

import com.github.f4b6a3.ulid.Ulid

opaque type TodoId = Ulid

opaque type TodoName = String

case class Todo(
    id: TodoId,
    name: TodoName,
    schedule: Schedule,
    notifyBefore: FiniteDuration,
    lastRun: Option[OffsetDateTime],
)

case class CreateTodo(
    name: TodoName,
    schedule: Schedule,
)

enum Schedule:
  case EverySecondFridayOfTheMonth(timeOfDay: LocalTime)

  private given CanEqual[DayOfWeek, DayOfWeek] =
    CanEqual.derived

  def shouldRun(
      lastRun: OffsetDateTime,
      now: OffsetDateTime,
  ): Boolean =
    this match
      case EverySecondFridayOfTheMonth(timeOfDay) =>
        now.getDayOfWeek == DayOfWeek.FRIDAY &&
        now.getDayOfMonth >= 8 &&
        now.getDayOfMonth <= 14 &&
        lastRun.getMonthValue < now.getMonthValue &&
        timeOfDay.getHour <= now.getHour &&
        timeOfDay.getMinute <= now.getMinute
