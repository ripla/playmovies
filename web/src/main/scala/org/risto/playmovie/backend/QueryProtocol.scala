package org.risto.playmovie.backend

/**
 * Message protocol for movie queries
 *
 * User: Risto Yrjänä
 * Date: 16.8.2013
 * Time: 19.51
 */
object QueryProtocol {

  case class Query(query: String)

  abstract class QueryResult

  final case class Success(name: String, year: Int, rating: Rating, service: String) extends QueryResult

  case object NotFound extends QueryResult

  case object NotAvailable extends QueryResult

}
