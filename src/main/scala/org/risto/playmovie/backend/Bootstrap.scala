package org.risto.playmovie.backend

import akka.kernel.Bootable
import akka.actor.{Props, ActorSystem}
import org.risto.playmovie.backend.imdb.ImdbSupervisor

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

    val querySupervisors = List(("imdb", Props(new ImdbSupervisor())))
    system.actorOf(Props(new QueryMaster(querySupervisors)))

  }

  def shutdown() {
    system.shutdown()
  }
}
