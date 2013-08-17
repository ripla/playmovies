package org.risto.playmovie.backend

/**
 * Created with IntelliJ IDEA.
 * User: ripla
 * Date: 16.8.2013
 * Time: 19.53
 * To change this template use File | Settings | File Templates.
 */
case class Rating(rating:Int) {
  if(rating <= 0 || rating > 10) throw new IllegalArgumentException(s"Illegal rating value $rating")
}
