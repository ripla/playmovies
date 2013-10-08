package org.risto.playmovie.backend.imdb

import akka.actor.{ActorLogging, Actor}
import akka.pattern.pipe
import akka.event.Logging

import scala.concurrent.Future
import scala.concurrent.duration._

import spray.http.HttpHeaders.`Content-Type`
import spray.http.ContentTypes.`application/json`
import spray.json.{JsonFormat, DefaultJsonProtocol}
import spray.can.Http
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import spray.util._
import spray.http._

import com.github.theon.uri.Uri._

object ImdbProtocol {

  case class ImdbResult(title: String, rating: Double, year: Int)

  case class ImdbResponse(result: Option[List[ImdbResult]], code: Option[Int] = None)

  case class ImdbQuery(query: String)

}

import org.risto.playmovie.backend.imdb.ImdbProtocol._

object ImdbJsonProtocol extends DefaultJsonProtocol {

  implicit val resultFormat = jsonFormat3(ImdbResult)
  implicit val responseFormat = jsonFormat2(ImdbResponse)
}

import org.risto.playmovie.backend.imdb.ImdbJsonProtocol._
import spray.httpx.SprayJsonSupport._

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 17.8.2013
 * Time: 20.12
 */
class ImdbWorker extends Actor with ActorLogging {

  //TODO maybe use the Spray URI classes?
  val endpointUri = "http://mymovieapi.com/" ? ("offset" -> 0) & ("plot" -> "none")

  implicit val system = context.system

  import system.dispatcher


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

  val pipeline: HttpRequest => Future[ImdbResponse] = sendReceive ~>
    mapEntityContentTypeToJson ~>
    // enable for debugging
    logResponse(log) ~>
    unmarshal[ImdbResponse]

  def receive = {

    case ImdbQuery(query) => {

      val uriWithQuery = endpointUri & ("q" -> query)
      val resultFuture: Future[ImdbResponse] = pipeline(Get(uriWithQuery))
      resultFuture pipeTo sender
    }
  }


}
