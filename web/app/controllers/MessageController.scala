package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import play.api.Routes
import org.risto.playmovie.common.Global

case class Message(value: String)

object MessageController extends Controller {

  implicit val fooWrites = Json.writes[Message]

  def getMessage = Action {
    Ok(Json.toJson(Message(s"Hello from ${Global.StringOption("").getOrElse("Common dependency working OK")}")))
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")(routes.javascript.MessageController.getMessage)).as(JAVASCRIPT)
  }

}