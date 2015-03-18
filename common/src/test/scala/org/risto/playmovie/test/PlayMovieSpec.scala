package org.risto.playmovie.test

import org.risto.playmovie.common.QueryProtocol
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec}
import akka.testkit.{CallingThreadDispatcher, TestKit, TestKitBase}
import akka.actor.{Props, ActorRef, Actor, ActorSystem}
import org.scalatest.mock.MockitoSugar

import scala.reflect.ClassTag

/**
 * Class combining common functionality for all specifications
 *
 * @author Risto Yrjänä
 */
class PlayMovieSpec(specName: String) extends FlatSpec with BeforeAndAfterAll with BeforeAndAfter with TestKitBase with MockitoSugar{

  // working around the problem that FlatSpec and TestKit are both classes
  // normally these come from the traits TestKit and ImplicitSender
  implicit def self = testActor

  implicit lazy val system = ActorSystem(specName)

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  def singleThreadActor[T <: Actor: ClassTag](creator: ⇒ T): Props = Props(creator).withDispatcher(CallingThreadDispatcher.Id)
}

class ActorForwarder(target: ActorRef) extends Actor {
  def receive = {
    case msg => target forward msg
  }
}

class NoopActor extends Actor {
  def receive = {
    case msg =>
  }
}

class EchoActor(defaultResponse: Any) extends Actor {
  def receive = {
    case anyMessage => sender() ! defaultResponse
  }
}