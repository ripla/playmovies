package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.{JsValue, Writes, Json}
import org.risto.playmovie.common.Rating
import org.risto.playmovie.common.QueryProtocol._

object SearchController extends Controller {

  implicit val ratingWrites: Writes[Rating] = new Writes[Rating]{
    override def writes(o: Rating): JsValue = Json.toJson(o.rating)
  }
  implicit val successWrites: Writes[Success] = Json.writes[Success]

  def search = Action {
    Ok(Json.toJson(Success(name = "Blade Runner", year = Some(1983), Rating(9), service = "The MovieDB", "123")))
  }
}