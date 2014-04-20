package controllers

import play.api._
import play.api.mvc._

object MainController extends Controller {
  def index() = Action {
    Ok(views.html.index("Hello from your Scala overlords"))
  }
}