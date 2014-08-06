package org.risto.playmovie.common

/**
 * Message protocol for movie queries
 *
 * User: Risto Yrjänä
 * Date: 16.8.2013
 * Time: 19.51
 */
object QueryProtocol {

  case class Query(query: String, uuid: String)

  abstract class QueryResult(service: String, uuid: String) {
    def success: Boolean
  }

  case class Success(name: String, year: Option[Int], rating: Rating, service: String, uuid: String) extends QueryResult(service, uuid) {
    override val success = true
  }

  abstract class Failure(service: String, uuid: String) extends QueryResult(service: String, uuid: String){
    override val success = false
  }

  case class NotAvailable(service: String, uuid: String) extends Failure(service: String, uuid: String)

  case class Unknown(service: String, uuid: String) extends Failure(service: String, uuid: String)

  case class NotFound(service: String, uuid: String) extends Failure(service: String, uuid: String)

  case class Unauthorized(service: String, uuid: String) extends Failure(service: String, uuid: String)
}
