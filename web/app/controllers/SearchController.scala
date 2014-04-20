package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.Json
import org.risto.playmovie.common.Global
import play.api.Routes

object SearchController extends Controller {

  implicit val fooWrites = Json.writes[Message]

  def search = Action {
    Ok(Json.toJson(Message(s"Hello from ${Global.StringOption("").getOrElse("Common dependency working OK")}")))
  }
}