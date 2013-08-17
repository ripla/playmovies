package org.risto.playmovie.backend.imdb

import org.risto.playmovie.backend.QuerySupervisor
import org.risto.playmovie.backend.QueryProtocol.Query

/**
 * Created with IntelliJ IDEA.
 * User: Risto Yrjänä
 * Date: 17.8.2013
 * Time: 20.12
 */
class ImdbSupervisor extends QuerySupervisor {

  override def receive = {
    case Query(queryString) =>
    case msg => super.receive(msg)
  }
}
