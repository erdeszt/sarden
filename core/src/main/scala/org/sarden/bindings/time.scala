package org.sarden.bindings

import doobie.{Get, Put}
import io.scalaland.chimney.partial.Result
import io.scalaland.chimney.{PartialTransformer, Transformer}
import org.sarden.InternalError
import zio.json.*
import zio.json.ast.Json

import java.time.*
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object time:

  export java.time.{Instant, LocalTime, OffsetDateTime, ZoneId}
  export java.util.concurrent.TimeUnit

  export scala.concurrent.duration.FiniteDuration

  case class InvalidLocalDateValueError(raw: String)
      extends InternalError(s"Invalid LocalDate format: ${raw}")

  case class InvalidTimeUnitError(raw: String)
      extends InternalError(s"Invalid TimeUnit format: ${raw}")

  // TODO: Use .mapOrFail to properly handle the errors
  given JsonDecoder[FiniteDuration] = JsonDecoder[Map[String, Json]].map: raw =>
    FiniteDuration(
      raw("length").as[Long].toOption.get,
      TimeUnitCodec.read(raw("unit").as[String].toOption.get).get,
    )

  given JsonEncoder[FiniteDuration] = JsonEncoder[Map[String, Json]].contramap:
    duration =>
      Map(
        "length" -> Json.Num(duration.length),
        "unit" -> Json.Str(TimeUnitCodec.write(duration.unit)),
      )

  given PartialTransformer[String, FiniteDuration] = PartialTransformer: raw =>
    Result.fromEitherString(raw.fromJson[FiniteDuration])

  given Transformer[FiniteDuration, String] = _.toJson

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

  given CanEqual[OffsetDateTime, OffsetDateTime] = CanEqual.derived

  given PartialTransformer[Long, OffsetDateTime] = PartialTransformer: raw =>
    Result.fromEitherString:
      Try(
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(raw), ZoneId.of("UTC")),
      ).toEither.left.map(_.getMessage)

  given Transformer[OffsetDateTime, Long] = _.toEpochSecond

  given instantGet: Get[Instant] =
    Get[Long].map(raw => Instant.ofEpochSecond(raw))

  given instantPut: Put[Instant] =
    Put[Long].contramap(instant => instant.getEpochSecond)

  given localDateGet: Get[LocalDate] =
    Get[String].map(raw =>
      raw
        .fromJson[LocalDate]
        .getOrElse(throw InvalidLocalDateValueError(raw)),
    )

  given PartialTransformer[String, LocalDate] = PartialTransformer: raw =>
    Result.fromEitherString(raw.fromJson)

  given Transformer[LocalDate, String] = _.toJson

  given localDatePut: Put[LocalDate] =
    Put[String].contramap(_.toJson)

  given CanEqual[TimeUnit, TimeUnit] = CanEqual.derived

  object TimeUnitCodec:
    def write: TimeUnit => String =
      case TimeUnit.NANOSECONDS  => "NANOSECONDS"
      case TimeUnit.MICROSECONDS => "MICROSECONDS"
      case TimeUnit.MILLISECONDS => "MILLISECONDS"
      case TimeUnit.SECONDS      => "SECONDS"
      case TimeUnit.MINUTES      => "MINUTES"
      case TimeUnit.HOURS        => "HOURS"
      case TimeUnit.DAYS         => "DAYS"

    def read: String => Option[TimeUnit] =
      case "NANOSECONDS"  => Some(TimeUnit.NANOSECONDS)
      case "MICROSECONDS" => Some(TimeUnit.MICROSECONDS)
      case "MILLISECONDS" => Some(TimeUnit.MILLISECONDS)
      case "SECONDS"      => Some(TimeUnit.SECONDS)
      case "MINUTES"      => Some(TimeUnit.MINUTES)
      case "HOURS"        => Some(TimeUnit.HOURS)
      case "DAYS"         => Some(TimeUnit.DAYS)
      case _              => None
