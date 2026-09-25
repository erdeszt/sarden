package gls

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import gls.domain.*

opaque type Port = Int
object Port extends NewtypeUnwrap[Port, Int] {
  def apply(raw: Int): Option[Port] = {
    if (raw > 0 && raw <= 65535) {
      Some(raw)
    } else {
      None
    }
  }
  given ConfigReader[Port] = ConfigReader[Port].emap { raw =>
    Port(raw) match {
      case None       => Left(CannotConvert(raw.toString, "Port", "Outside of valid range"))
      case Some(port) => Right(port)
    }
  }
}

case class WebConfig(
    port: Port,
) derives ConfigReader

case class AppConfig(
    env: String, // TODO: Enum + proper parsing
    web: WebConfig,
) derives ConfigReader
