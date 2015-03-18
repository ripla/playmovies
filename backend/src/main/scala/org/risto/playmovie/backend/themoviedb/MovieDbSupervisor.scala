package org.risto.playmovie.backend.themoviedb

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.{AskTimeoutException, ask, pipe}
import akka.routing.FromConfig
import akka.util.Timeout
import org.risto.playmovie.backend.themoviedb.MovieDbProtocol.MovieDbResponse
import org.risto.playmovie.common.QueryProtocol._
import org.risto.playmovie.common.{QueryProtocol, Rating}
import spray.http.StatusCodes

import scala.concurrent.Future
import scala.concurrent.duration._

object MovieDbSupervisor {
  val getWorkerProps =
    ("moviedbrouter", Props(classOf[MovieDbWorker])
      .withRouter(FromConfig))

  val supervisorProps = ("moviedbsupervisor", Props(classOf[MovieDbSupervisor]))
}


class MovieDbSupervisor(workerProps: (String, Props)) extends Actor with ActorLogging {

  val worker = context.actorOf(workerProps._2, workerProps._1)
  //for the futures
  implicit val system = context.system

  def this() = this(MovieDbSupervisor.getWorkerProps)

  def receive = LoggingReceive {
    case Query(query, uuid) => {
      //TODO cache

      implicit val timeout = Timeout(5 seconds)
      import context.dispatcher

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

  import system.dispatcher

  def mapRating(d: Double): Rating = Rating(Math.ceil(d).toInt)
}
