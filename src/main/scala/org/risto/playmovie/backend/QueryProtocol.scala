package org.risto.playmovie.backend

/**
 * Created with IntelliJ IDEA.
 * User: ripla
 * Date: 16.8.2013
 * Time: 19.51
 * To change this template use File | Settings | File Templates.
 */
object QueryProtocol {

  case class Query(query: String)

  case class QueryResult(name:String, year:String, rating:Rating)
}
