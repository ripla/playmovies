package org.risto.playmovie.backend

import akka.actor.{Actor, ActorLogging}
import org.risto.playmovie.common.QueryProtocol.QueryResult

/**
 * Actor that simply logs all the query results. Mainly for diagnostic purposes
 *
 * @author Risto Yrjänä
 */
class ResultLoggingActor extends Actor with ActorLogging {

  def receive = {
    case result: QueryResult => log.debug(s"Received query result $result")
  }
}
