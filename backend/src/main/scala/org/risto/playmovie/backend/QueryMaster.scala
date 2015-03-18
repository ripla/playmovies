package org.risto.playmovie.backend

import akka.actor._
import akka.event.LoggingReceive
import org.risto.playmovie.common.QueryProtocol

/**
 * Handles all the incoming queries and sends them on to the pre-configured supervisors.
 * The results are handled by the provided result actors.
 *
 * @param queryActors
 * @param resultActors
 */
class QueryMaster(queryActors: List[Props], resultActors: List[Props]) extends Actor with ActorLogging {
  require(queryActors != null && queryActors.nonEmpty, "At least one query actor required")
  require(resultActors != null && resultActors.nonEmpty, "At least one result actor required")

  var childIndex = 0

  val supervisors: List[ActorRef] = queryActors map (supervisorActorProps => createActor(supervisorActorProps))

  val writers: List[ActorRef] = resultActors map (resultActorProps => createActor(resultActorProps))

  def receive = LoggingReceive {
    case query: QueryProtocol.Query => supervisors foreach (_ ! query)

    case queryResult: QueryProtocol.QueryResult => writers foreach (_ ! queryResult)
  }

  def createActor(supervisorActorProps: Props): ActorRef = {
    val newActor = context.actorOf(supervisorActorProps, supervisorActorProps.actorClass().getSimpleName + childIndex)
    childIndex += 1
    newActor
  }
}
