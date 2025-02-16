package org.sarden.web.routes.pages

import scalatags.Text
import scalatags.Text.all.*

def layout(pageContent: Text.TypedTag[String]*): Text.TypedTag[String] =
  html(
    lang := "en",
    attr("data-bs-theme") := "dark",
    head(
      scalatags.Text.tags2.title("Sarden"),
      meta(charset := "utf-8"),
      meta(
        name := "viewport",
        content := "width=device-width, initial-scale=1",
      ),
      meta(name := "theme-color", content := "#712cf9"),
      script(src := "/assets/js/bootstrap.min.js"),
      link(href := "/assets/css/bootstrap.min.css", rel := "stylesheet"),
      link(href := "/assets/css/main.css", rel := "stylesheet"),
    ),
    body(
      tag("nav")(
        cls := "navbar navbar-expand-md navbar-dark fixed-top bg-dark",
        div(
          cls := "container-fluid",
          a(href := "/", cls := "navbar-brand", "Sarden"),
          button(
            cls := "navbar-toggler",
            `type` := "button",
            attr("data-bs-toggle") := "collapse",
            attr("data-bs-target") := "#navbarSupportedContent",
          ),
          div(
            cls := "collapse navbar-collapse",
            id := "navbarSupportedContent",
            ul(
              cls := "navbar-nav me-auto mb-2 mb-lg-0",
              li(
                cls := "navbar-item",
                a(cls := "nav-link", href := "/plants", "Plants"),
              ),
              li(
                cls := "navbar-item",
                a(cls := "nav-link", href := "/sowlog", "Sow log"),
              ),
              li(
                cls := "navbar-item",
                a(cls := "nav-link", href := "/todos", "TODOs"),
              ),
            ),
            form(
              cls := "d-flex",
              role := "search",
              input(
                cls := "form-control me-2",
                `type` := "search",
                placeholder := "Search",
                attr("aria-label") := "Search",
              ),
              button(
                cls := "btn btn-outline-success",
                `type` := "submit",
                "Search",
              ),
            ),
          ),
        ),
      ),
      pageContent,
    ),
  )
