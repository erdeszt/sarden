package org.sarden.core

import java.time.{Instant, LocalTime, OffsetDateTime, ZoneId}
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

import doobie.{Get, Put}
import io.scalaland.chimney.partial.Result
import io.scalaland.chimney.{PartialTransformer, Transformer}
import zio.json.*
import zio.json.ast.Json

import org.sarden.core.SystemErrors.InvalidTimeUnitError

object time:

  export java.time.{Instant, LocalTime, OffsetDateTime, ZoneId}
  export java.util.concurrent.TimeUnit

  export scala.concurrent.duration.FiniteDuration

  given JsonDecoder[FiniteDuration] = JsonDecoder[Map[String, Json]].map: raw =>
    FiniteDuration(
      raw("length").as[Long].toOption.get,
      TimeUnitCodec.read(raw("unit").as[String].toOption.get).toOption.get,
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

    def read: String => Either[InvalidTimeUnitError, TimeUnit] =
      case "NANOSECONDS"  => Right(TimeUnit.NANOSECONDS)
      case "MICROSECONDS" => Right(TimeUnit.MICROSECONDS)
      case "MILLISECONDS" => Right(TimeUnit.MILLISECONDS)
      case "SECONDS"      => Right(TimeUnit.SECONDS)
      case "MINUTES"      => Right(TimeUnit.MINUTES)
      case "HOURS"        => Right(TimeUnit.HOURS)
      case "DAYS"         => Right(TimeUnit.DAYS)
      case other          => Left(InvalidTimeUnitError(other))
