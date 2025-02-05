package org.sarden.core.domain.todo

import java.time.{DayOfWeek, Instant, LocalTime, OffsetDateTime, ZoneId}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

import com.github.f4b6a3.ulid.Ulid
import doobie.{Get, Put, Read, Write}
import zio.json.*
import zio.json.ast.Json

opaque type TodoId = Ulid

object TodoId:
  inline def apply(raw: Ulid): TodoId = raw

  given CanEqual[TodoId, TodoId] = CanEqual.derived
  given Get[TodoId] = Get[String].map(raw => Ulid.from(raw))
  given Put[TodoId] = Put[String].contramap(_.unwrap.toString)
  given JsonDecoder[TodoId] =
    JsonDecoder[String].map(raw => TodoId(Ulid.from(raw)))
  given JsonEncoder[TodoId] =
    JsonEncoder[String].contramap(id => id.unwrap.toString)

extension (id: TodoId) def unwrap: Ulid = id

opaque type TodoName = String

object TodoName:
  inline def apply(raw: String): TodoName = raw

  given CanEqual[TodoName, TodoName] = CanEqual.derived
  given Get[TodoName] = Get[String].map(raw => raw)
  given Put[TodoName] = Put[String].contramap(raw => raw)
  given JsonDecoder[TodoName] = JsonDecoder[String].map(raw => raw)
  given JsonEncoder[TodoName] = JsonEncoder[String].contramap(raw => raw)

extension (name: TodoName) def unwrap: String = name

case class Todo(
    id: TodoId,
    name: TodoName,
    schedule: TodoSchedule,
    notifyBefore: FiniteDuration,
    lastRun: Option[OffsetDateTime],
) derives JsonCodec,
      Read,
      Write

given JsonDecoder[FiniteDuration] = JsonDecoder[Map[String, Json]].map { raw =>
  FiniteDuration(
    raw("length").as[Long].toOption.get,
    TimeUnitPickle.read(raw("unit").as[String].toOption.get).toOption.get,
  )
}
given JsonEncoder[FiniteDuration] = JsonEncoder[Map[String, Json]].contramap {
  duration =>
    Map(
      "length" -> Json.Num(duration.length.toDouble),
      "unit" -> Json.Str(TimeUnitPickle.write(duration.unit)),
    )
}

given getFiniteDuration: Get[FiniteDuration] =
  Get[String].map(raw => raw.fromJson[FiniteDuration].toOption.get)
given putFiniteDuration: Put[FiniteDuration] =
  Put[String].contramap(duration => duration.toJson)

given getOffsetDateTime: Get[OffsetDateTime] =
  Get[Long].map(raw =>
    OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.of("UTC")),
  )
given putOffsetDateTime: Put[OffsetDateTime] =
  Put[Long].contramap(dateTime => dateTime.toEpochSecond)

case class CreateTodo(
    name: TodoName,
    schedule: TodoSchedule,
    notifyBefore: FiniteDuration,
) derives JsonCodec

given JsonDecoder[OffsetDateTime] = JsonDecoder[Long].map { raw =>
  OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.of("UTC"))
}
given JsonEncoder[OffsetDateTime] =
  JsonEncoder[Long].contramap(dateTime => dateTime.toInstant.getEpochSecond)

given JsonDecoder[LocalTime] = JsonDecoder[Map[String, Json]].map { raw =>
  LocalTime.of(
    raw("hour").as[Int].toOption.get,
    raw("minute").as[Int].toOption.get,
  )
}
given JsonEncoder[LocalTime] = JsonEncoder[Map[String, Json]].contramap {
  localTime =>
    Map(
      "hour" -> Json.Num(localTime.getHour),
      "minute" -> Json.Num(localTime.getMinute),
    )
}

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

enum TodoSchedule derives JsonCodec, CanEqual:
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

object TodoSchedule:
  given get: Get[TodoSchedule] =
    Get[String].map(raw => raw.fromJson[TodoSchedule].toOption.get)
  given put: Put[TodoSchedule] =
    Put[String].contramap(schedule => schedule.toJson)
