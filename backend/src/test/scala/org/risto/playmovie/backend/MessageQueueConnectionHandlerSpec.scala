package org.risto.playmovie.backend

import org.risto.playmovie.test.PlayMovieSpec
import com.rabbitmq.client.{Channel, ConnectionFactory, Connection}
import akka.actor.Props
import org.mockito.Mockito.when

/**
 * @author Risto Yrjänä
 */
class MessageQueueConnectionHandlerSpec extends PlayMovieSpec("MessageQueueConnectionHandleSpec") {

  val connectionMock: Connection = mock[Connection]

  val factoryMock: ConnectionFactory = mock[ConnectionFactory]

  override protected def beforeAll(): Unit = {
    MessageQueueConnectionHandler.connectionFactory = factoryMock
    when(factoryMock.newConnection()).thenReturn(connectionMock)
  }

  behavior of "A MessageQueueConnectionHandleSpec"

  it should "return connection when started" in {
    val channelMock = mock[Channel]
    when(connectionMock.createChannel()).thenReturn(channelMock)

    system.actorOf(Props(new MessageQueueConnectionHandler(testActor)))

    expectMsg(MessageQueueConnectionHandler.Connection(channelMock))
  }

  it should "retry connection if first attempt fails" in {
    val channelMock = mock[Channel]
    //interestingly enough, this needs to be an unchecked exception
    when(connectionMock.createChannel())
    .thenThrow(new RuntimeException())
    .thenReturn(channelMock)

    system.actorOf(Props(new MessageQueueConnectionHandler(testActor)))

    expectMsg(MessageQueueConnectionHandler.Connection(channelMock))
  }

  it should "return connection after multiple retries" in {
    val channelMock = mock[Channel]
    //interestingly enough, this needs to be an unchecked exception
    when(connectionMock.createChannel())
    .thenThrow(new RuntimeException())
    .thenThrow(new RuntimeException())
    .thenThrow(new RuntimeException())
    .thenReturn(channelMock)

    system.actorOf(Props(new MessageQueueConnectionHandler(testActor)))

    expectMsg(MessageQueueConnectionHandler.Connection(channelMock))
  }

}
