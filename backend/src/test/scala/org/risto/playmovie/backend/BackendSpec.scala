package org.risto.playmovie.backend

import akka.actor.Props
import com.rabbitmq.client.RpcClient
import org.risto.playmovie.backend.themoviedb.MovieDbSupervisor
import org.risto.playmovie.test.PlayMovieSpec

import scala.util.Try

/**
 * @author risto
 */
class BackendSpec extends PlayMovieSpec("BackendSpec") {

  behavior of "the Backend"

  it should "be composed of layers" in {

    val message = """{"query":"matrix"}"""

    val querySupervisors: List[(String, Props)] = List(MovieDbSupervisor.supervisorProps)

    val queryMaster = system.actorOf(Props(new QueryMaster(querySupervisors)), "querymaster")

    val mqAdapter = system.actorOf(Props(new MessageQueueAdapter), "mqAdapter")

    val queryAdapter = system.actorOf(Props(new QueryMqAdapter(queryMaster, mqAdapter)), "queryAdapter")

    val channel = MessageQueueAdapter.createQueryChannel().get

    val rpc: RpcClient = new RpcClient(channel, "", MessageQueueAdapter.queue, 3000)
    val response = Try(rpc.stringCall(message))

    Console.print(response)
    assert(response.isSuccess)
    Console.println(response.get)
  }
}
