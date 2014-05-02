package org.risto.playmovie.backend

import akka.actor.Props
import scala.concurrent.duration._
import org.risto.playmovie.test.PlayMovieSpec
import org.risto.playmovie.backend.themoviedb.MovieDbProtocol.{MovieDbQuery, MovieDbResponse, MovieDbResult}
import org.joda.time.DateTime
import org.risto.playmovie.backend.themoviedb.MovieDbWorker

/**
 * User: Risto Yrjänä
 * Date: 19.8.2013
 * Time: 13.41
 */
class MovieDbWorkerSpec extends PlayMovieSpec("MovieDbWorkerSpec") {

  behavior of "An MovieDbWorker"

  it should "return correct information on query 'Blade Runner'" in {
    val movieDbWorker = system.actorOf(Props(new MovieDbWorker))
    movieDbWorker ! MovieDbQuery("Blade Runner")
    val Date = DateTime.parse("1982-06-25")
    //expectMsg(5 seconds, MovieDbResponse(Some(List(MovieDbResult("Blade Runner", 7.5, DateTime.parse("1982-06-25")))), None))
    expectMsgPF(5 seconds) {
      case MovieDbResponse(Some(List(MovieDbResult("Blade Runner", 7.5, Date), _*)), None) =>
    }
  }

  it should "return an empty result on query 'foobar'" in {
    val movieDbWorker = system.actorOf(Props(new MovieDbWorker))
    movieDbWorker ! MovieDbQuery("foobar")
    expectMsg(5 seconds, MovieDbResponse(None, None))
  }
}
