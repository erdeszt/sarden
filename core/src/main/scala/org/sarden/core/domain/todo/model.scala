package org.sarden.core.domain.todo

import java.time.{DayOfWeek, LocalTime, OffsetDateTime}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{FiniteDuration, TimeUnit}

import com.github.f4b6a3.ulid.Ulid
import upickle.default.{ReadWriter, readwriter}

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

given ReadWriter[LocalTime] = readwriter[ujson.Value].bimap[LocalTime](
  value => ujson.Obj(("hour", value.getHour), ("minute", value.getMinute)),
  json => LocalTime.of(json("hour").num.toInt, json("minute").num.toInt),
)

given CanEqual[TimeUnit, TimeUnit] = CanEqual.derived

class InvalidTimeUnitException(rawValue: String)
    extends Exception(s"Invalid TimeUnit: `${rawValue}`")

object TimeUnitPickle:
  def write: TimeUnit => String = {
    case TimeUnit.NANOSECONDS  => "NANOSECONDS"
    case TimeUnit.MICROSECONDS => "MICROSECONDS"
    case TimeUnit.MILLISECONDS => "MILLISECONDS"
    case TimeUnit.SECONDS      => "SECONDS"
    case TimeUnit.MINUTES      => "MINUTES"
    case TimeUnit.HOURS        => "HOURS"
    case TimeUnit.DAYS         => "DAYS"
  }

  def read: String => Either[InvalidTimeUnitException, TimeUnit] = {
    case "NANOSECONDS"  => Right(TimeUnit.NANOSECONDS)
    case "MICROSECONDS" => Right(TimeUnit.MICROSECONDS)
    case "MILLISECONDS" => Right(TimeUnit.MILLISECONDS)
    case "SECONDS"      => Right(TimeUnit.SECONDS)
    case "MINUTES"      => Right(TimeUnit.MINUTES)
    case "HOURS"        => Right(TimeUnit.HOURS)
    case "DAYS"         => Right(TimeUnit.DAYS)
    case other          => Left(InvalidTimeUnitException(other))
  }

given ReadWriter[FiniteDuration] =
  readwriter[ujson.Value].bimap[FiniteDuration](
    value =>
      ujson.Obj(
        ("length", value.length),
        ("unit", TimeUnitPickle.write(value.unit)),
      ),
    json =>
      TimeUnitPickle.read(json("unit").str) match
        case Left(error) => throw error
        case Right(unit) => FiniteDuration(json("length").num.toInt, unit),
  )

enum Schedule derives ReadWriter:
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