package gls

import scala.language.experimental.saferExceptions

def check[Error <: Exception](condition: Boolean, error: Error): Unit throws
  Error = {
  if (!condition) {
    throw error
  }
}
