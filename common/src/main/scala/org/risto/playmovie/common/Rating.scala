package org.risto.playmovie.common

/**
 * Movie rating from 0 to 10
 *
 * @author Risto Yrjänä
 */
case class Rating(rating: Int) {
  require((rating > 0 || rating <= 10), s"Illegal rating value $rating")
}
