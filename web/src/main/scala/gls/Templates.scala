package gls

import java.nio.file.Path

import gg.jte.output.StringOutput
import gg.jte.resolve.DirectoryCodeResolver
import gg.jte.{ContentType, TemplateEngine}

trait Templates {
  def render(template: String): String
}

class JteTemplates(templateDirectory: Path) extends Templates {

  private val resolver = DirectoryCodeResolver(templateDirectory)
  private val engine = TemplateEngine.create(resolver, ContentType.Html)

  override def render(template: String): String = {
    val output = StringOutput()

    engine.render(template, Map.empty, output)

    output.toString
  }
}
