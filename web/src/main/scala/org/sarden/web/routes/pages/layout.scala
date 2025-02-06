package org.sarden.web.routes.pages

import scalatags.Text
import scalatags.Text.all.*

def layout(content: Text.TypedTag[String]): Text.TypedTag[String] =
  html(
    head(
      scalatags.Text.tags2.title("Sarden"),
      script(src := "/assets/js/bootstrap.min.js"),
      link(href := "/assets/css/bootstrap.min.css", rel := "stylesheet"),
      link(href := "/assets/css/main.css", rel := "stylesheet"),
    ),
    content,
  )
