package org.risto.playmovie.backend

import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}
import org.risto.playmovie.backend.imdb.ImdbSupervisor
import org.risto.playmovie.backend.themoviedb.MovieDbSupervisor


object Bootstrap {
  def main(args: Array[String]) = new Bootstrap().startup()
}

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 17.8.2013
 * Time: 20.12
 */
class Bootstrap extends Bootable {

  val system = ActorSystem("playmovie-backend-kernel")

  def startup() {
    //TODO read config
    //TODO get actors from config

    val querySupervisors: List[(String, Props)] = List(MovieDbSupervisor.supervisorProps)

    val queryMaster = system.actorOf(Props(new QueryMaster(querySupervisors)), "querymaster")

    val mqAdapter = system.actorOf(Props(new MessageQueueAdapter), "mqAdapter")

    system.actorOf(Props(new QueryMqAdapter(queryMaster, mqAdapter)), "queryAdapter")
  }

  def shutdown() {
    system.shutdown()
  }
}
