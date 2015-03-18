package org.risto.playmovie.backend.mockactors

import akka.actor.{Cancellable, Actor, ActorRef}
import org.risto.playmovie.common.QueryProtocol

object QueryEmittingActor {
  case object Ping
}

/**
 * Mock actor that emits a valid Query at 5 second intervals
 */
class QueryEmittingActor(target: ActorRef) extends Actor {
  require(target != null)

  var ticker: Cancellable = _

  override def preStart(): Unit = {
    import scala.concurrent.duration._
    import context.dispatcher
    ticker = context.system.scheduler.schedule(5 seconds, 5 seconds, context.self, QueryEmittingActor.Ping)
  }

  override def postStop(): Unit = {
    ticker.cancel()
  }

  def receive = {
    case QueryEmittingActor.Ping => target ! QueryProtocol.Query("the Matrix", "42")
  }
}
