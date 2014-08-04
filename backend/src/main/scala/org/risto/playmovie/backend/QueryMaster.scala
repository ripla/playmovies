package org.risto.playmovie.backend

import akka.actor._
import akka.event.LoggingReceive
import org.risto.playmovie.backend.QueryMasterProtocol.{RemoveSupervisor, AddSupervisor}
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.pipe
import scala.concurrent.Future
import org.risto.playmovie.common.QueryProtocol
import org.risto.playmovie.common.QueryProtocol.QueryResult

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 8.8.2013
 * Time: 23.16
 */
class QueryMaster(initialSupervisors: List[(String, Props)] = List.empty) extends Actor with ActorLogging {

  def this() = {
    this(List.empty)
  }

  var supervisors: List[ActorRef] = initialSupervisors map {
    case (id: String, props: Props) => context.actorOf(props, id)

  }

  def receive = LoggingReceive {
    case query: QueryProtocol.Query => {
      if(supervisors.isEmpty) {
        log.info(s"No query supervisors defined, discarding query: $query")
      }else {
        implicit val system = context.system
        import system.dispatcher
        implicit val queryTimeout = Timeout(5 seconds)

        val resultListFutures: Iterable[Future[QueryResult]] = supervisors.map(
          supervisor => (supervisor ? query).mapTo[QueryResult])

        //This is highly inefficient. With multiple result sources, an alternative approach is needed.
        resultListFutures foreach (result => result pipeTo sender)
      }
    }

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
