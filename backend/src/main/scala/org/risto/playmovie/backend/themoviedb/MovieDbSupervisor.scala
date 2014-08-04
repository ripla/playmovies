package org.risto.playmovie.backend.themoviedb

import akka.actor.{ActorLogging, Actor, Props}
import akka.event.LoggingReceive
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
  val getWorkerProps =
    ("moviedbrouter", Props(classOf[MovieDbWorker])
      .withRouter(FromConfig))

  val supervisorProps = ("moviedbsupervisor", Props(classOf[MovieDbSupervisor]))
}


class MovieDbSupervisor(workerProps: (String, Props)) extends Actor with ActorLogging {

  def this() = this(MovieDbSupervisor.getWorkerProps)

  val worker = context.actorOf(workerProps._2, workerProps._1)

  def mapRating(d: Double): Rating = Rating(Math.ceil(d).toInt)

  //for the futures
  implicit val system = context.system

  import system.dispatcher

  def receive = LoggingReceive {
    case Query(query, uuid) => {
      //TODO cache

      implicit val timeout = Timeout(5 seconds)

      val response: Future[MovieDbResponse] = (worker ? MovieDbProtocol.MovieDbQuery(query)).mapTo[MovieDbResponse]

      val queryResultHappyPaths: Future[QueryResult] = response map {
        case MovieDbResponse(Some(result :: _), None) => Success(result.title, result.release_date.map(_.year().get()), mapRating(result
          .vote_average), "The Movie DB", uuid)

        case MovieDbResponse(None, Some(code)) => code match {

          case StatusCodes.Unauthorized.intValue => {
            QueryProtocol.Unauthorized("The Movie DB", uuid)
          }
        }
      }

      val resultFuture: Future[QueryResult] = queryResultHappyPaths recover {
        case _: AskTimeoutException => NotAvailable("The Movie DB", uuid)
        case other => {
          log.error(other, "Unknown throwable from The MovieDB query")
          Unknown("The Movie DB", uuid)
        }
      }

      resultFuture pipeTo sender
    }
  }
}
