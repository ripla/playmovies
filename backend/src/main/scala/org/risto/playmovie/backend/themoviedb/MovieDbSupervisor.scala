package org.risto.playmovie.backend.themoviedb

import akka.actor.{ActorLogging, Actor, Props}
import org.risto.playmovie.common.{Rating, QueryProtocol}
import akka.routing.FromConfig
import akka.pattern.{AskTimeoutException, ask, pipe}
import QueryProtocol._
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.util.Timeout
import org.risto.playmovie.backend.themoviedb.MovieDbProtocol.MovieDbResponse
import spray.http.StatusCodes

object MovieDbSupervisor {
  def getProps =
    ("moviedbrouter", Props(new MovieDbWorker())
      .withRouter(FromConfig)
      .withDispatcher("moviedb.workerDispatcher"))

}


class MovieDbSupervisor(workerProps: (String, Props)) extends Actor with ActorLogging {

  val worker = context.actorOf(workerProps._2, workerProps._1)

  def mapRating(d: Double): Rating = Rating(Math.ceil(d).toInt)

  //for the futures
  implicit val system = context.system

  import system.dispatcher

  def receive = {
    case Query(query) => {
      //TODO cache

      implicit val timeout = Timeout(5 seconds)

      val response: Future[MovieDbResponse] = (worker ? MovieDbProtocol.MovieDbQuery(query)).mapTo[MovieDbResponse]

      val queryResultHappyPaths: Future[QueryResult] = response map {
        case MovieDbResponse(Some(result :: _), None) => Success(result.title, result.release_date.year().get(), mapRating(result
          .vote_average), "The Movie DB")

        case MovieDbResponse(None, Some(code)) => code match {

          case StatusCodes.Unauthorized.intValue => {
            QueryProtocol.Unauthorized
          }
        }
      }

      val resultFuture: Future[QueryResult] = queryResultHappyPaths recover {
        case _: AskTimeoutException => NotAvailable
        case other => {
          log.error(other, "Unknown throwable from The MovieDB query")
          Unknown
        }
      }

      resultFuture pipeTo sender
    }
  }
}
