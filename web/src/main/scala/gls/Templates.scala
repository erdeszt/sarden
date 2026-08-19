package gls

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.io.ClassPathTemplateLoader

trait Templates {
  def render(template: String): String
  def render[T](template: String, payload: T): String
}

// TODO: Caching option
class HandlebarsTemplates private (handlebars: Handlebars) extends Templates {

  override def render(template: String): String = {
    render(template, null)
  }

  override def render[T](template: String, payload: T): String = {
    val compiledTemplate = handlebars.compile(template)

    compiledTemplate.apply(payload)
  }

}

object HandlebarsTemplates {

  def create(): HandlebarsTemplates = {
    val loader = ClassPathTemplateLoader()

    loader.setPrefix("/templates")
    loader.setSuffix(".hbs")

    val handlebars = Handlebars(loader)

    HandlebarsTemplates(handlebars)
  }

}
