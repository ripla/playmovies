package org.risto.playmovie.test

import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import akka.testkit.{TestKit, TestKitBase}
import akka.actor.{ActorRef, Actor, ActorSystem}

/**
 * Class combining common functionality for all specifications
 * User: Risto Yrjänä
 * Date: 22.8.2013
 * Time: 18.07
 */
class PlayMovieSpec(specName: String) extends FlatSpec with BeforeAndAfterAll with TestKitBase {

  // working around the problem that FlatSpec and TestKit are both classes
  // normally these come from the traits TestKit and ImplicitSender
  implicit def self = testActor

  implicit lazy val system = ActorSystem(specName)

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }



  class ActorForwarder(target: ActorRef) extends Actor {
    def receive = {
      case msg => target forward msg
    }
  }
}
