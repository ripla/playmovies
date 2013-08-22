package org.risto.playmovie.backend

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.risto.playmovie.backend.imdb.ImdbWorker
import org.risto.playmovie.backend.imdb.ImdbProtocol.{ImdbResponse, ImdbQuery, ImdbResult}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.util.Timeout

/**
 * User: Risto Yrjänä
 * Date: 19.8.2013
 * Time: 13.41
 */
class ImdbWorkerSpec extends TestKit(ActorSystem
  .create("ImdbWorkerSpec")) with FlatSpec with BeforeAndAfterAll with ImplicitSender {

  override def afterAll() = {
    system.shutdown()
  }

  behavior of "An ImdbWorker"

  it should "return correct information on query 'Blade Runner'" in {
    val imdbWorker = system.actorOf(Props(new ImdbWorker))
    imdbWorker ! ImdbQuery("Blade Runner")
    expectMsg(5 seconds, ImdbResponse(List(ImdbResult("Blade Runner", 8.3, 1982))))
  }

  it should "return a NotFound result on query 'foobar'" in {
    val imdbWorker = system.actorOf(Props(new ImdbWorker))
    imdbWorker ! ImdbQuery("foobar")
    expectMsg(5 seconds, ImdbResponse(List(ImdbResult("Blade Runner", 8.3, 1982))))
  }
}
