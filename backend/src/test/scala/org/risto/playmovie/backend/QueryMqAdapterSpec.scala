package org.risto.playmovie.backend

import akka.actor.Props
import org.risto.playmovie.common.QueryProtocol.Success
import org.risto.playmovie.common.{QueryProtocol, Rating}
import org.risto.playmovie.test.PlayMovieSpec

/**
 * @author risto
 */
class QueryMqAdapterSpec extends PlayMovieSpec("queryMqAdapterSpec") {

  behavior of "Query to MQ adapter"

  it should "register itself to the provided MQ adapter on start" in {

    val mqAdapter = testActor

    val queryMqAdapter = system.actorOf(Props(new QueryMqAdapter(null, mqAdapter)))

    expectMsg(MQAdapterProtocol.Register(queryMqAdapter))
  }

  it should "unregister itself from the provided MQ adapter on stop" in {

    val mqAdapter = testActor

    val queryMqAdapter = system.actorOf(Props(new QueryMqAdapter(null, mqAdapter)))
    system.stop(queryMqAdapter)

    expectMsg(MQAdapterProtocol.Register(queryMqAdapter))
    expectMsg(MQAdapterProtocol.UnRegister(queryMqAdapter))
  }

  it should "Convert incoming JSON query to a QueryProtocol.Query" in {

    val testMessage = """{ "query": "foo" }"""

    val queryMaster = testActor

    val queryMqAdapter = system.actorOf(Props(classOf[QueryMqAdapter], queryMaster, testActor))

    queryMqAdapter ! MQAdapterProtocol.Message(testMessage, Some("id"))
    expectMsg(MQAdapterProtocol.Register(queryMqAdapter))

    expectMsg(QueryProtocol.Query("foo", "id"))
  }

  it should "Convert outgoing query success to JSON" in {

    val successMsg: Success = QueryProtocol.Success("the Matrix", Some(1999), Rating(8), "MovieDB", "id")
    val expectedJsonResult = """{"name":"the Matrix","year":1999,"rating":{"rating":8},"service":"MovieDB","uuid":"id"}"""

    val mqAdapter = testActor

    val queryMqAdapter = system.actorOf(Props(classOf[QueryMqAdapter], null, mqAdapter))
    queryMqAdapter ! successMsg
    expectMsg(MQAdapterProtocol.Register(queryMqAdapter))

    expectMsg(MQAdapterProtocol.SendMessage(expectedJsonResult, Some("id")))
  }
}
