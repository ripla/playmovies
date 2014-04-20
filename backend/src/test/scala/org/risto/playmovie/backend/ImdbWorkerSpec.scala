package org.risto.playmovie.backend

import akka.actor.Props
import org.risto.playmovie.backend.imdb.ImdbWorker
import org.risto.playmovie.backend.imdb.ImdbProtocol.{ImdbQuery, ImdbResponse, ImdbResult}
import scala.concurrent.duration._
import org.risto.playmovie.test.PlayMovieSpec

/**
 * User: Risto Yrjänä
 * Date: 19.8.2013
 * Time: 13.41
 */
class ImdbWorkerSpec extends PlayMovieSpec("ImdbWorkerSpec") {

  behavior of "An ImdbWorker"

  ignore should "return correct information on query 'Blade Runner'" in {
    val imdbWorker = system.actorOf(Props(new ImdbWorker))
    imdbWorker ! ImdbQuery("Blade Runner")
    expectMsg(5 seconds, ImdbResponse(Some(List(ImdbResult("Blade Runner", 8.3, 1982)))))
  }

  ignore should "return a 404 result on query 'foobar'" in {
    val imdbWorker = system.actorOf(Props(new ImdbWorker))
    imdbWorker ! ImdbQuery("foobar")
    expectMsg(5 seconds, ImdbResponse(None, Some(404)))
  }
}
