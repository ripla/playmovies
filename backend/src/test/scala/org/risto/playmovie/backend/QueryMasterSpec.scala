package org.risto.playmovie.backend

import akka.actor._
import org.risto.playmovie.common.QueryProtocol.Query
import org.risto.playmovie.common.{QueryProtocol, Rating}
import org.risto.playmovie.test.{ActorForwarder, EchoActor, NoopActor, PlayMovieSpec}

/**
 * Specification for [[QueryMaster]]
 *
 * @author Risto Yrjänä
 */
class QueryMasterSpec extends PlayMovieSpec("QueryMasterSpec") {

  val successMessage = QueryProtocol.Success("test", None, Rating(2), "testService", "42")
  val notfoundMessage = QueryProtocol.NotFound("TestService", "42")

  behavior of "A QueryMaster"

  it should "forward queries to a configured QuerySupervisor" in {
    val querySupervisors = List(Props(new ActorForwarder(testActor)))
    val resultWriters = List(Props[NoopActor])
    val queryMaster = system.actorOf(Props(new QueryMaster(querySupervisors, resultWriters)))

    queryMaster ! Query("test1", "123")
    expectMsg(Query("test1", "123"))

    queryMaster ! Query("test2", "123")
    expectMsg(Query("test2", "123"))
  }

  it should "forward the results to result writer" in {
    val querySupervisors = List(Props[NoopActor])
    val resultWriters = List(Props(new ActorForwarder(testActor)))
    val queryMaster = system.actorOf(Props(new QueryMaster(querySupervisors, resultWriters)))

    queryMaster ! notfoundMessage
    expectMsg(notfoundMessage)

    queryMaster ! successMessage
    expectMsg(successMessage)
  }

  it should "work with multiple query and result actors" in {
    //needs to be single threaded execution
    //otherwise message order would not be consistent
    val querySupervisors = List(singleThreadActor(new EchoActor(notfoundMessage)), singleThreadActor(new EchoActor(successMessage)))
    val resultWriters = List(singleThreadActor(new ActorForwarder(testActor)), singleThreadActor(new ActorForwarder(testActor)))
    val queryMaster = system.actorOf(singleThreadActor(new QueryMaster(querySupervisors, resultWriters)))

    queryMaster ! Query("test1", "123")

    expectMsg(notfoundMessage)
    expectMsg(notfoundMessage)
    expectMsg(successMessage)
    expectMsg(successMessage)
  }
}

