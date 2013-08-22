package org.risto.playmovie.backend

/**
 * User: Risto Yrjänä
 * Date: 16.8.2013
 * Time: 19.53
 */
case class Rating(rating: Int) {
  if (rating <= 0 || rating > 10) throw new IllegalArgumentException(s"Illegal rating value $rating")
}
