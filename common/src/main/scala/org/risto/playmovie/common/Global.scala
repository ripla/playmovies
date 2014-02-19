package org.risto.playmovie.common

/**
 *
 * @author Risto Yrjänä
 */
object Global {

  object StringOption {
    def apply(value: String): Option[String] = if ("" == value) None else Option(value)

  }

  object Implicits {
    implicit def stringToOption(value: String): Option[String] = StringOption(value)
  }

}
