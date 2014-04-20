package org.risto.playmovie

import com.typesafe.config.ConfigFactory

/**
 * Created by risto on 18.4.2014.
 */
object PlayMovieConfig {

  lazy val get = ConfigFactory.load("custom").withFallback(ConfigFactory.load())
}
