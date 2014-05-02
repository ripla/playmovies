package org.risto.playmovie.backend.themoviedb

import akka.actor.{ActorLogging, Actor}
import akka.pattern.pipe

import scala.concurrent.Future

import spray.http.ContentTypes.`application/json`
import spray.json._
import spray.client.pipelining._

import com.github.theon.uri.Uri._
import spray.http.{HttpEntity, HttpRequest, HttpResponse}
import org.joda.time.DateTime
import org.risto.playmovie.PlayMovieConfig
import org.risto.playmovie.common.QueryProtocol
import spray.httpx.UnsuccessfulResponseException


object MovieDbProtocol {

  case class MovieDbResult(title: String, vote_average: Double, release_date: DateTime)

  case class MovieDbResponse(results: Option[List[MovieDbResult]], code: Option[Int])

  case class MovieDbQuery(query: String)

}

import org.risto.playmovie.backend.themoviedb.MovieDbProtocol._

object MovieDbJsonProtocol extends DefaultJsonProtocol {

  implicit object ColorJsonFormat extends RootJsonFormat[DateTime] {
    val MovieDbDatePattern = """(\d{4})-(\d{2})-(\d{2})""".r


    def write(d: DateTime) = JsArray(JsString(d.toString("yyyy-MM-dd")))

    def read(value: JsValue) = value match {
      case JsString(MovieDbDatePattern(year, month, date)) =>
        new DateTime(year.toInt, month.toInt, date.toInt, 0, 0)
      case _ => deserializationError("Date expected")
    }
  }

  implicit val resultFormat = jsonFormat3(MovieDbResult)
  implicit val responseFormat = jsonFormat2(MovieDbResponse)
}


/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 17.8.2013
 * Time: 20.12
 */
class MovieDbWorker extends Actor with ActorLogging {

  //TODO maybe use the Spray URI classes?
  val endpointUri = PlayMovieConfig.get.getString("moviedb.endpointUri") + "/3/search/movie" ? ("api_key" -> PlayMovieConfig.get.getString("moviedb.apikey"))

  implicit val system = context.system

  import system.dispatcher

  import org.risto.playmovie.backend.themoviedb.MovieDbJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  /*
  this is a bit non-intuitive, but since unmarshalling uses the content-type in the entity / response body,
  we need to map that and not the content-type in the header

  val jsonHeader: `Content-Type` = `Content-Type`(`application/json`)
  val mapContentTypeToJson: HttpResponse => HttpResponse = response => {
    val test = response.headers map (_.lowercaseName)
    response.withHeaders(response.headers map {
      case HttpHeader("content-type", _) => jsonHeader
      case other => other
    })
  }
  */

  val mapEntityContentTypeToJson: HttpResponse => HttpResponse = response =>
    response.withEntity(HttpEntity(`application/json`, response.entity.data))

  val pipeline: HttpRequest => Future[MovieDbResponse] = sendReceive ~>
    //    mapEntityContentTypeToJson ~>
    // enable for debugging
    logResponse(log) ~>
    unmarshal[MovieDbResponse]

  def receive = {

    case MovieDbQuery(query) => {

      val uriWithQuery = endpointUri & ("query" -> query)
      val resultFuture: Future[MovieDbResponse] = pipeline(Get(uriWithQuery))
      resultFuture recover {
        case ure: UnsuccessfulResponseException => {
          log.error(s"Query $query failed with ${ure.response.status}. Details: ${ure.response.entity}")
          MovieDbResponse(results = None, code = Some(ure.response.status.intValue))
        }
        case otherFail => {
          log.error(s"Query $query failed with $otherFail.")
          QueryProtocol.Unknown
        }
      } map{
        case MovieDbResponse(Some(Nil), code) => MovieDbResponse(None, code)
        case other => other
      } pipeTo sender
    }
  }


}
