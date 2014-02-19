package org.risto.playmovie.backend

import akka.actor.{ActorSystem, ActorRef, Props}
import akka.testkit.{TestActor, TestProbe, TestActorRef, TestKit}
import org.risto.playmovie.backend.imdb.ImdbSupervisor
import org.risto.playmovie.backend.imdb.ImdbProtocol.{ImdbResponse, ImdbResult, ImdbQuery}
import akka.testkit.TestActor.AutoPilot
import org.risto.playmovie.test.PlayMovieSpec

/**
 * User: Risto Yrjänä
 * Date: 19.8.2013
 * Time: 13.41
 */
class ImdbSupervisorSpec extends PlayMovieSpec("ImdbSupervisorSpec") {

  behavior of "An ImdbSupervisor"

  it should "return a correctly mapped Success from ImdbResult" in {
    val resultFromRemote = ImdbResponse(Some(List(ImdbResult("title", 4.2, 1984))))

    val echoProbe = TestProbe()
    echoProbe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
        case ImdbQuery("test") => sender ! resultFromRemote; TestActor.NoAutoPilot
      }
    })

    //val testWorkerProps = ("testWorker", Props(new ActorForwarder(testActor)))
    val supervisor = TestActorRef[ImdbSupervisor](Props(new ImdbSupervisor(("testWorker", Props(new ActorForwarder(echoProbe.ref))))))

    supervisor ! QueryProtocol.Query("test")
    expectMsg(QueryProtocol.Success("title", 1984, Rating(5), "IMDB"))
  }
}
