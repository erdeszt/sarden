package gls

import neotype.Newtype
import neotype.interop.pureconfig.given
import pureconfig.ConfigReader

type Port = Port.Type
object Port extends Newtype[Int]:
  override inline def validate(input: Int): Boolean =
    input > 0 && input < 65535

case class WebConfig(
    port: Port,
) derives ConfigReader

case class AppConfig(
    env: String, // TODO: Enum + proper parsing
    web: WebConfig,
) derives ConfigReader
