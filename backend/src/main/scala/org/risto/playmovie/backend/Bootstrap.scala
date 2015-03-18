package org.risto.playmovie.backend

import akka.actor.{ActorSystem, Props}
import akka.kernel.Bootable
import org.risto.playmovie.backend.mockactors.{MockQueryActor, QueryEmittingActor}


object Bootstrap {
  def main(args: Array[String]) = new Bootstrap().startup()
}

/**
 * Main class to run when starting the backend
 */
class Bootstrap extends Bootable {

  val system = ActorSystem("playmovie-backend-kernel")

  def startup() {
    //TODO read config and pass it on
    //TODO get actors from config
    //val supervisors = List(Props[MovieDbSupervisor])
    val supervisors = List(Props[MockQueryActor])
    val resultWriters = List(Props[ResultLoggingActor])

    val queryMaster = system.actorOf(Props(new QueryMaster(supervisors, resultWriters)), "querymaster")

    //val mqAdapter = system.actorOf(Props(new MessageQueueAdapter), "mqAdapter")

    //system.actorOf(Props(new QueryMqAdapter(queryMaster, mqAdapter)), "queryAdapter")

    system.actorOf(Props(new QueryEmittingActor(queryMaster)))
  }

  def shutdown() {
    system.shutdown()
  }
}
