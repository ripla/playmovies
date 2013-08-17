package org.risto.playmovie.backend

import akka.actor.{PoisonPill, Props, ActorRef, Actor}
import org.risto.playmovie.backend.QueryMasterProtocol.{RemoveSupervisor, AddSupervisor}

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 8.8.2013
 * Time: 23.16
 */
class QueryMaster(initialSupervisors: List[(String, Props)] = List.empty) extends Actor {

  def this() = {
    this(List.empty)
  }

  var supervisors: List[ActorRef] = initialSupervisors map {
    case (id: String, props: Props) => context.actorOf(props, id)
  }

  def receive = {
    case query: QueryProtocol.Query => supervisors foreach (supervisor => supervisor.forward(query))
    case AddSupervisor(id, props) => supervisors = context.actorOf(props, id) :: supervisors
    case RemoveSupervisor(id) => context.child(id) foreach {
      child =>
        supervisors = supervisors.filterNot(supervisor => supervisor == child)
        child ! PoisonPill
    }
  }
}

object QueryMasterProtocol {

  case class AddSupervisor(id: String, props: Props)

  case class RemoveSupervisor(id: String)

}
