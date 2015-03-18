package org.risto.playmovie.backend

import akka.actor.{ActorRef, Props}
import akka.testkit.{TestActorRef, TestProbe, TestActor}
import org.joda.time.DateTime
import org.risto.playmovie.backend.imdb.ImdbSupervisor
import org.risto.playmovie.backend.imdb.ImdbProtocol.{ImdbQuery, ImdbResult, ImdbResponse}
import org.risto.playmovie.backend.themoviedb.MovieDbProtocol.{MovieDbQuery, MovieDbResult, MovieDbResponse}
import org.risto.playmovie.backend.themoviedb.MovieDbSupervisor
import org.risto.playmovie.test.{ActorForwarder, PlayMovieSpec}
import org.risto.playmovie.common.{Rating, QueryProtocol}
import akka.testkit.TestActor.AutoPilot

/**
 * @author Risto Yrjänä
 */
class MovieDbSupervisorSpec extends PlayMovieSpec("MovieDbSupervisorSpec") {

  behavior of "An MovieDbSupervisor"

  ignore should "return a correctly mapped Success from MovieDbResult" in {
    val resultFromRemote = MovieDbResponse(Some(List(MovieDbResult("title", 4.2, Some(new DateTime().withYear(1984))))), None)

    val echoProbe = TestProbe()
    echoProbe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
        case MovieDbQuery("test") => sender ! resultFromRemote; TestActor.NoAutoPilot
      }
    })

    //val testWorkerProps = ("testWorker", Props(new ActorForwarder(testActor)))
    val supervisor = TestActorRef[MovieDbSupervisor](Props(new MovieDbSupervisor(("testWorker", Props(new ActorForwarder(echoProbe.ref))))))

    supervisor ! QueryProtocol.Query("test", "123")
    expectMsg(QueryProtocol.Success("title", Some(1984), Rating(5), "IMDB", "123"))
  }
}
