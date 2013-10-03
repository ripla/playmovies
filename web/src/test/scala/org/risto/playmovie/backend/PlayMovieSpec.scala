package org.risto.playmovie.backend

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{Actor, ActorRef}

/**
 * Trait combining common functionality for all specifications
 * User: Risto Yrjänä
 * Date: 22.8.2013
 * Time: 18.07
 */
trait PlayMovieSpec extends FlatSpec with BeforeAndAfterAll with ImplicitSender{
  self: TestKit =>

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  class ActorForwarder(target: ActorRef) extends Actor {
    def receive = {
      case msg => target forward msg
    }
  }
}
