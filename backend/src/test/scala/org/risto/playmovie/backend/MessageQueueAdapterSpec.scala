package org.risto.playmovie.backend

import akka.actor.{PoisonPill, ActorRef, Props}
import org.risto.playmovie.test.PlayMovieSpec
import com.rabbitmq.client._
import akka.testkit.{TestActor, TestProbe}
import akka.testkit.TestActor.AutoPilot
import org.risto.playmovie.backend.MQAdapterProtocol.Message
import org.risto.playmovie.backend.MQAdapterProtocol.SendMessage

/**
 * @author Risto Yrjänä
 */
class MessageQueueAdapterSpec extends PlayMovieSpec("MessageQueueAdapterSpec") {


  var channel: Channel = _
  val message = """{'query':'matrix'}"""
  var messageAdapter: ActorRef = _

  before {
    channel = MessageQueueAdapter.createQueryChannel().get

    messageAdapter = system.actorOf(Props(new MessageQueueAdapter()))
  }

  behavior of "A MessageQueueAdapter"


  it should "send a query message to listeners when it receives message from the MQ" in {
    //setup
    messageAdapter ! MQAdapterProtocol.Register(testActor)

    //exercise
    channel.basicPublish("", MessageQueueAdapter.queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())

    //verify
    expectMsg(MQAdapterProtocol.Message(message, None))

    //tear down
    messageAdapter ! PoisonPill
  }

  it should "work with RPC-style semantics asynchronously" in {
    //setup
    val channel = MessageQueueAdapter.createQueryChannel().get
    channel.queueDeclare("response_queue", true, false, false, null)
    val message = """{'query':'matrix'}"""
    val expectedResultFromRemote = """{'title':'the Matrix','rating':'2','year':'2000'}"""
    val expectedMessageId = "1"
    val rpcMessageProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder().correlationId(expectedMessageId)
                               .replyTo("response_queue").build()

    var response: String = null
    var correlationId: String = null

    channel.basicConsume("response_queue", true, new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
        correlationId = properties.getCorrelationId
        response = new String(body)
      }
    })


    val messageAdapter = system.actorOf(Props(new MessageQueueAdapter()))

    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
        case Message(message, uuid) => probe.send(sender, SendMessage(expectedResultFromRemote, uuid));
          TestActor
          .NoAutoPilot
      }
    })

    messageAdapter ! MQAdapterProtocol.Register(probe.ref)

    //exercise
    channel.basicPublish("", MessageQueueAdapter.queue, rpcMessageProperties, message.getBytes())

    //verify
    Thread.sleep(500)
    assert(expectedResultFromRemote === response)
    assert(expectedMessageId === correlationId)

    //tear down
    channel.queueDelete("response_queue")
    messageAdapter ! PoisonPill
  }

  it should "work with standard MQ RPC" in {
    //setup
    val channel = MessageQueueAdapter.createQueryChannel().get
    val message = """{'query':'matrix'}"""
    val resultFromRemote = """{'title':'the Matrix','rating':'2','year':'2000'"""
    val messageAdapter = system.actorOf(Props(new MessageQueueAdapter()))

    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
        case Message(message, uuid) => probe.send(sender, SendMessage(resultFromRemote, uuid)); TestActor.NoAutoPilot
        //case _ =>
      }
    })

    messageAdapter ! MQAdapterProtocol.Register(probe.ref)

    //exercise
    val rpc: RpcClient = new RpcClient(channel, "", MessageQueueAdapter.queue, 3000)
    val response = rpc.stringCall(message)

    //verify

    assert(resultFromRemote === response)

    //tear down
    messageAdapter ! PoisonPill
  }

  it should "send multiple responses to single query" in {
    //setup
    val channel = MessageQueueAdapter.createQueryChannel().get
    channel.queueDeclare("response_queue", true, false, false, null)
    val message = """{'query':'matrix'}"""
    val expectedResultFromRemote1 = """{'title':'the Matrix','rating':'2','year':'2000'}"""
    val expectedResultFromRemote2 = """{'title':'the Matrix Reloaded','rating':'3','year':'2003'}"""

    val expectedMessageId = "1"
    val rpcMessageProperties = MessageProperties.PERSISTENT_TEXT_PLAIN.builder().correlationId(expectedMessageId)
                               .replyTo("response_queue").build()

    var responses: List[String] = List.empty
    var correlationIds: List[String] = List.empty

    channel.basicConsume("response_queue", true, new DefaultConsumer(channel) {
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
        correlationIds = correlationIds :+ properties.getCorrelationId
        responses = responses :+ new String(body)
      }
    })

    val messageAdapter = system.actorOf(Props(new MessageQueueAdapter()))

    val probe = TestProbe()
    probe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): AutoPilot = msg match {
        case Message(message, uuid) => {
          probe.send(sender, SendMessage(expectedResultFromRemote1, uuid))
          probe.send(sender, SendMessage(expectedResultFromRemote2, uuid))
          TestActor.NoAutoPilot
        }
      }
    })

    messageAdapter ! MQAdapterProtocol.Register(probe.ref)

    //exercise
    channel.basicPublish("", MessageQueueAdapter.queue, rpcMessageProperties, message.getBytes())

    //verify
    Thread.sleep(500)
    assert(expectedResultFromRemote1 === responses.head)
    assert(expectedResultFromRemote1 === responses.tail.head)
    assert(expectedMessageId === correlationIds.head)
    assert(expectedMessageId === correlationIds.tail.head)

    //tear down
    channel.queueDelete("response_queue")
    messageAdapter ! PoisonPill
  }
}
