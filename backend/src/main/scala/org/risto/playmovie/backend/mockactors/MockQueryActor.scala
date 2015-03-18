package org.risto.playmovie.backend.mockactors

import akka.actor.Actor
import org.risto.playmovie.common.{QueryProtocol, Rating}

/**
 * Actor that always returns success for queries. Mainly for configuration testing
 */
class MockQueryActor extends Actor {

  def receive ={
    case query: QueryProtocol.Query => sender() ! QueryProtocol.Success("the Matrix", Some(1999), Rating(8), "MockService", query.uuid)
  }
}
