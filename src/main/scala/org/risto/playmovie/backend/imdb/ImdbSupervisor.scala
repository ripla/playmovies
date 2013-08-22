package org.risto.playmovie.backend.imdb

import akka.actor.{Actor, Props}
import org.risto.playmovie.backend.QueryProtocol.Query
import akka.routing.FromConfig
import akka.pattern.{AskTimeoutException, ask, pipe}
import org.risto.playmovie.backend.QueryProtocol._
import org.risto.playmovie.backend.imdb.ImdbProtocol.ImdbResponse
import scala.concurrent.Future
import org.risto.playmovie.backend.Rating

object ImdbSupervisor {
  def getProps =
    ("imdbrouter", Props(new ImdbWorker())
      .withRouter(FromConfig)
      .withDispatcher("imdb.workerDispatcher"))

}


class ImdbSupervisor(workerProps: (String, Props)) extends Actor {

  val worker = context.actorOf(workerProps._2, workerProps._1)

  def mapRating(d: Double): Rating = Rating(Math.ceil(d).toInt)

  def receive = {
    case Query(query) => {
      //TODO cache

      val imdbResponse: Future[ImdbResponse] = (worker ? ImdbProtocol.ImdbQuery(query)).mapTo[ImdbResponse]

      val queryResultHappyPaths: Future[QueryResult] = imdbResponse map {
        case ImdbResponse(Some(result :: _), None) => Success(result.title, result.year, mapRating(result
          .rating), "IMDB")
        case ImdbResponse(_, Some(404)) => NotFound
      }

      val resultFuture: Future[QueryResult] = queryResultHappyPaths recover {
        case _: AskTimeoutException => NotAvailable
      }

      resultFuture pipeTo sender
    }
  }
}
