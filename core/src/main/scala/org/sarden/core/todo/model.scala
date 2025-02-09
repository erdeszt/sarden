package org.sarden.core.todo

import java.time.{DayOfWeek, LocalTime, OffsetDateTime}

import scala.concurrent.duration.FiniteDuration

import neotype.*

import org.sarden.core.ulid.*

type TodoId = TodoId.Type
object TodoId extends UlidNewtype

type TodoName = TodoName.Type
object TodoName extends Newtype[String]:
  given CanEqual[TodoName, TodoName] = CanEqual.derived

case class Todo(
    id: TodoId,
    name: TodoName,
    schedule: TodoSchedule,
    notifyBefore: FiniteDuration,
    lastRun: Option[OffsetDateTime],
)

case class CreateTodo(
    name: TodoName,
    schedule: TodoSchedule,
    notifyBefore: FiniteDuration,
)

enum TodoSchedule derives CanEqual:
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
