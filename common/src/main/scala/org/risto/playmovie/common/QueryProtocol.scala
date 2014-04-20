package org.risto.playmovie.common

/**
 * Message protocol for movie queries
 *
 * User: Risto Yrjänä
 * Date: 16.8.2013
 * Time: 19.51
 */
object QueryProtocol {

  case class Query(query: String)

  abstract class QueryResult {
    def success: Boolean
  }

  case class Success(name: String, year: Int, rating: Rating, service: String) extends QueryResult {
    override val success = true
  }

  abstract class Failure extends QueryResult{
    override val success = false
  }

  case object NotAvailable extends Failure

  case object Unknown extends Failure

  case object NotFound extends Failure

  case object Unauthorized extends Failure
}
