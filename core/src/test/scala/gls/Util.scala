package gls

import scala.language.experimental.saferExceptions
import scala.reflect.ClassTag

object Util {

  def yolo[A, Error <: Exception](action: => A throws Error): A = {
    try {
      action
    } catch {
      case error: Exception =>
        throw RuntimeException(s"yolo failed: ${error.getMessage}", error)
    }
  }

  def expect[Error <: Exception: ClassTag]: Expector[Error] = Expector()

  class Expector[Error <: Exception](using errorTag: ClassTag[Error]) {
    def apply[OtherErrors <: Exception, A](
        action: => A throws Error | OtherErrors,
    ): Unit = {
      try {
        val result = action
        throw RuntimeException(
          s"did not receive expected error: ${errorTag.runtimeClass.getName}, got: ${result}",
        )
      } catch {
        case error: Exception =>
          if (errorTag.runtimeClass.isAssignableFrom(error.getClass)) {
            ()
          } else {
            throw RuntimeException(
              s"unexpected error: ${error.getMessage}",
              error,
            )
          }
      }
    }
  }

}
