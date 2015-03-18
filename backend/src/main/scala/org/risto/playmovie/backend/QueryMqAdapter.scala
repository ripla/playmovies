package org.risto.playmovie.backend

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import org.risto.playmovie.backend.MQAdapterProtocol.{Message, SendMessage}
import org.risto.playmovie.common.{QueryProtocol, Rating}
import spray.json._

object QuerySuccessProtocol extends DefaultJsonProtocol {
  implicit val ratingFormat = jsonFormat1(Rating)
  //implicit val successFormat = jsonFormat5(QueryProtocol.Success)
  //name: String, year: Int, rating: Rating, service: String, uuid: String
  implicit val successFormat = jsonFormat(QueryProtocol.Success.apply, "name", "year", "rating", "service", "uuid")
}

/**
 * @author Risto Yrjänä
 */
class QueryMqAdapter(queryMaster: ActorRef, mqAdapter: ActorRef) extends Actor with ActorLogging {

  override def preStart() = {
    mqAdapter ! MQAdapterProtocol.Register(context.self)
  }

  import QuerySuccessProtocol._

  override def postStop() = {
    mqAdapter ! MQAdapterProtocol.UnRegister(context.self)
  }

  def receive = LoggingReceive {
    case Message(message: String, Some(uuid)) => queryMaster ! QueryProtocol
      .Query(message.parseJson.asJsObject.fields("query").compactPrint.filterNot(_ == '"'), uuid)

    case result: QueryProtocol.Success => mqAdapter ! SendMessage(result.toJson.compactPrint, Some(result.uuid))

    //TODO
    case failure: QueryProtocol.Failure => log.error(s"Sending failed searches to UI not supported. Failed: $failure")
  }


}
