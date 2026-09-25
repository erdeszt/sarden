package gls.domain.plant

import scala.util.Try

import upickle.default.*

import gls.domain.*

case class Names(
    english: String,
    hungarian: String,
    latin: String,
) derives Reader,
      Writer

opaque type Family = String
object Family extends Newtype[Family, String] {
  given ReadWriter[Family] = upickle.readwriter[String].bimap(_.unwrap, Family(_))
}

case class Plant(
    names: Names,
    family: Family,
    properties: Map[String, String],
    description: Vector[String],
) derives Reader,
      Writer

sealed trait ParserPhase
case class NoPlant() extends ParserPhase
case class ParsingPlant(plant: Plant) extends ParserPhase {
  def addProperty(key: String, value: String): ParserPhase = {
    copy(plant = plant.copy(properties = plant.properties + (key -> value)))
  }
  def addDescription(newDescription: String): ParserPhase = {
    copy(plant = plant.copy(description = plant.description :+ newDescription))
  }
}

case class ParserState(
    phase: ParserPhase,
    plants: Vector[Plant],
    errors: Vector[ParseError],
    line: Int,
) {
  def appendError(error: ParseError): ParserState = {
    copy(errors = errors :+ error)
  }
  def appendError(kind: ParseErrorKind, line: String): ParserState = {
    copy(errors = errors :+ ParseError(kind, line, this.line))
  }
}

object ParserState {
  def empty: ParserState = {
    ParserState(NoPlant(), Vector.empty, Vector.empty, 1)
  }
}

case class ParseError(kind: ParseErrorKind, line: String, lineNumber: Int) {
  override def toString: String = {
    s"ParseError: ${kind} at ${lineNumber}. Content: ${line}"
  }
}

enum ParseErrorKind {
  case NameExpectedGotProperty()
  case NameExpectedGotGarbage()
  case BadNamesFormat()
  case BadFamilyFormat()
  case BadPropertyFormat()

  override def toString: String = {
    this match {
      case NameExpectedGotProperty() => "Expected a name, got a property"
      case NameExpectedGotGarbage()  => "Expected a name, got garbage"
      case BadNamesFormat()          => "Incorrectly formatted name"
      case BadFamilyFormat()         => "Incorrectly formatted family"
      case BadPropertyFormat()       => "Incorrectly formatted property"
    }
  }
}

def parse(raw: String): (Vector[Plant], Vector[ParseError]) = {
  val result =
    raw.split('\n').foldLeft(ParserState.empty) { (state, rawLine: String) =>
      val line = rawLine.trim
      val lineParseResult = state.phase match {
        case NoPlant() =>
          if (line.isEmpty || line.startsWith("=")) {
            state
          } else if (line(0).isLetter) {
            val rawNames = line.split('/')

            rawNames.toList match {
              case english :: hungarian :: latinAndFamily :: Nil =>
                if (!latinAndFamily.contains("(") || !latinAndFamily.trim.endsWith(")")) {
                  state.appendError(ParseErrorKind.BadFamilyFormat(), line)
                } else {
                  val latinAndFamilyParts =
                    latinAndFamily.dropRight(1).split('(')

                  latinAndFamilyParts.toList match {
                    case latin :: family :: Nil =>
                      state.copy(
                        phase = ParsingPlant(
                          Plant(
                            Names(english.trim, hungarian.trim, latin.trim),
                            Family(family.trim),
                            Map.empty,
                            Vector.empty,
                          ),
                        ),
                      )
                    case _ =>
                      state.appendError(ParseErrorKind.BadFamilyFormat(), line)
                  }
                }
              case _ =>
                state.appendError(ParseErrorKind.BadNamesFormat(), line)
            }
          } else if (line.startsWith("-")) {
            state.appendError(ParseErrorKind.NameExpectedGotProperty(), line)
          } else {
            state.appendError(ParseErrorKind.NameExpectedGotGarbage(), line)
          }
        case parsingPhase @ ParsingPlant(plant) =>
          if (line.startsWith("=")) {
            state
          } else if (line.isEmpty) {
            state.copy(
              phase = NoPlant(),
              plants = state.plants :+ plant,
            )
          } else if (line.startsWith("-")) {
            val propertyParts = line.drop(1).trim.split(":")

            propertyParts.toList match {
              case description :: Nil =>
                state.copy(phase = parsingPhase.addDescription(description.trim))
              case key :: value :: Nil =>
                state.copy(phase = parsingPhase.addProperty(key.trim, value.trim))
              case _ =>
                state.appendError(ParseErrorKind.BadPropertyFormat(), line)
            }
          } else {
            state.appendError(ParseErrorKind.BadPropertyFormat(), line)
          }
      }

      lineParseResult.copy(line = state.line + 1)
    }

  (result.plants, result.errors)
}

def render(plants: Vector[Plant]): String = {
  val plantGroups = plants.groupBy { (plant: Plant) =>
    plant.properties.getOrElse("layer", "other")
  }

  val builder = List("root", "ground cover", "shrub", "small tree", "large tree", "other").foldLeft(StringBuilder()) {
    (buffer, layer: String) =>
      val plantsInLayer = plantGroups.getOrElse(layer, Vector.empty[Plant])

      if (plantsInLayer.isEmpty) {
        buffer
      } else {
        buffer.append(s"\n== ${layer.capitalize} ==\n\n${plantsInLayer.map(renderPlant).mkString("\n\n")}")
      }
  }

  builder.toString
}

def renderPlant(plant: Plant): String = {
  val properties = plant.properties.map((key, value) => s"- ${key}: ${value}").mkString("\n")
  val descriptions = plant.description.map(description => s"- ${description}").mkString("\n")
  s"""${plant.names.english}/${plant.names.hungarian}/${plant.names.latin}(${plant.family})
     |${properties}
     |${descriptions}""".stripMargin
}

def toJson(plants: Vector[Plant]): String = {
  upickle.write(plants.toList)
}

def fromJson(raw: String): Try[Vector[Plant]] = {
  Try(upickle.read[Vector[Plant]](raw))
}
