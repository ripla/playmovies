package org.risto.playmovie.test

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec}
import akka.testkit.{TestKit, TestKitBase}
import akka.actor.{ActorRef, Actor, ActorSystem}
import org.scalatest.mock.MockitoSugar

/**
 * Class combining common functionality for all specifications
 * User: Risto Yrjänä
 * Date: 22.8.2013
 * Time: 18.07
 */
class PlayMovieSpec(specName: String) extends FlatSpec with BeforeAndAfterAll with BeforeAndAfter with TestKitBase with MockitoSugar{

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
