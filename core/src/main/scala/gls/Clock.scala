package gls

import java.time.Instant

trait Clock {
  def now(): Instant
}

class JavaTimeClock extends Clock {
  
  override def now(): Instant = {
    Instant.now()
  }
  
}
