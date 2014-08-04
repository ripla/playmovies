package org.risto.playmovie.backend.imdb

import akka.actor.{Actor, Props}
import org.risto.playmovie.common.{Rating, QueryProtocol}
import QueryProtocol.Query
import akka.pattern.{AskTimeoutException, ask, pipe}
import QueryProtocol._
import org.risto.playmovie.backend.imdb.ImdbProtocol.ImdbResponse
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import akka.routing.FromConfig

object ImdbSupervisor {
  def getWorkerProps =
    ("imdbrouter", FromConfig.props(Props(new ImdbWorker())))
}


class ImdbSupervisor(workerProps: (String, Props)) extends Actor {

  val worker = context.actorOf(workerProps._2, workerProps._1)

  def mapRating(d: Double): Rating = Rating(Math.ceil(d).toInt)

  //for the futures
  implicit val system = context.system
  import system.dispatcher

  def receive = {
    case Query(query, uuid) => {
      //TODO cache

      implicit val imdbTimeout = Timeout(5 seconds)

      val imdbResponse: Future[ImdbResponse] = (worker ? ImdbProtocol.ImdbQuery(query)).mapTo[ImdbResponse]

      val queryResultHappyPaths: Future[QueryResult] = imdbResponse map {
        case ImdbResponse(Some(result :: _), None) => Success(result.title, Some(result.year), mapRating(result
          .rating), "IMDB", uuid)
        case ImdbResponse(_, Some(404)) => NotFound("IMDB", uuid)
      }

      val resultFuture: Future[QueryResult] = queryResultHappyPaths recover {
        case _: AskTimeoutException => NotAvailable("IMDB", uuid)
      }

      resultFuture pipeTo sender
    }
  }
}
