package org.risto.playmovie.backend.imdb

import akka.actor.{Actor, Props}
import org.risto.playmovie.backend.QueryProtocol.Query
import akka.routing.FromConfig


object ImdbSupervisor {
  def getProps =
    ("imdbrouter", Props(new ImdbWorker())
      .withRouter(FromConfig)
      .withDispatcher("imdb.workerDispatcher"))

}


class ImdbSupervisor(workerProps: (String, Props)) extends Actor {

  context.actorOf(workerProps._2, workerProps._1)

  def receive = {
    case Query(query) =>
  }
}
