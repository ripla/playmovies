package controllers

import play.api.Routes
import play.api.mvc.{Controller, Action}
import play.api.libs.json.Json

/**
 * Created by risto on 17.4.2014.
 */
object JavascriptController extends Controller {

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(Routes.javascriptRouter("jsRoutes")(SearchController.search)).as(JAVASCRIPT)
  }


}
