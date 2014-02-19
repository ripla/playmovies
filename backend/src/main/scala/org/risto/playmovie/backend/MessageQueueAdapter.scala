package org.risto.playmovie.backend

import akka.actor.{PoisonPill, ActorLogging, ActorRef, Actor}
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}

import com.rabbitmq.client._
import scala.collection.mutable.ListBuffer
import org.risto.playmovie.backend.MQAdapterProtocol.{SendMessage, UnRegister, Register, Message}
import akka.event.LoggingReceive
import org.risto.playmovie.common.Global.Implicits._

/**
 * Messaging protocol for communicating with [[MessageQueueAdapter]]
 */
object MQAdapterProtocol {

  case class Register(actor: ActorRef)

  case class UnRegister(actor: ActorRef)

  case class Message(message: String, uuid: Option[String])

  case class SendMessage(message: String, uuid: Option[String])

}

object MessageQueueAdapter {

  val exchange: String = "playExchange"

  val queue: String = "playQueryQueue"

  //val routingKey: String = "playRoutingkey"

  def createQueryChannel(): Channel = {
    createChannel(queue)
  }

  def createChannel(queue: String): Channel = {
    val conf = ConfigFactory.load().withFallback(ConfigFactory.empty().withValue("CLOUDAMQP_URL", ConfigValueFactory.fromAnyRef("amqp://guest:guest@localhost")))
    val uri = conf.getString("CLOUDAMQP_URL")

    val factory = new ConnectionFactory()
    factory.setUri(uri)
    //Try(factory.newConnection()) flatmap(connection => Try(connection.createChannel()))
    val connection = factory.newConnection()
    connection.createChannel()

  }
}

/**
 * Generic Actor that acts as a bridge between an AMQP and the Actor system. Handles messages in both directions, as well as replies.
 *
 * @author Risto Yrjänä
 */
class MessageQueueAdapter extends Actor with ActorLogging {

  var listeners: ListBuffer[ActorRef] = ListBuffer.empty

  var idReplyToQueue: Map[String, String] = Map.empty

  val channel = MessageQueueAdapter.createQueryChannel()

  val consumer = new DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]) = {
      log.debug(s"Received AMQP message: $consumerTag, $envelope, $properties, $body")

      val messageId: Option[String] = properties.getCorrelationId
      val replyTo: Option[String] = properties.getReplyTo

      if (messageId.isDefined && replyTo.isDefined)
        idReplyToQueue += messageId.get -> properties.getReplyTo
      else if (messageId.isDefined || replyTo.isDefined)
        log.error(s"Both replyTo and messageId must be defined. Got replyTo: $replyTo and messageId: $messageId")

      self ! Message(new String(body), messageId)
    }

    override def handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException) {
      log.debug(s"Received AMQP shutdown signal: $consumerTag, $sig")

      self ! PoisonPill
    }
  }


  override def preStart(): Unit = {
    log.debug(s"${this.getClass.getSimpleName} prestart")
    channel.queueDeclare(MessageQueueAdapter.queue, true, false, false, null)
    channel.basicConsume(MessageQueueAdapter.queue, true, consumer)
  }


  override def postStop(): Unit = {
    channel.close()
  }

  def getReplyQueue(id: String): String = idReplyToQueue.get(id) match {
    case Some(replyToQueue) => replyToQueue
    case None => throw new IllegalStateException(s"No replyToQueue found for message id $id")
  }

  def sendReplyMessage(message: String, uuid: String): Unit = {
    channel.basicPublish("", getReplyQueue(uuid), MessageProperties.PERSISTENT_TEXT_PLAIN.builder().correlationId(uuid).build(), message.getBytes())
  }

  def sendMessage(message: String): Unit = {
    channel.basicPublish("", MessageQueueAdapter.queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())
  }

  def receive = {
    LoggingReceive {
      case Register(actor) => {
        listeners += actor
        log.debug(s"Registered new listener, current listeners: $listeners")
      }
      case UnRegister(actor) => listeners -= actor
      case incomingMessage: Message => {
        log.debug(s"Sending $incomingMessage to $listeners")
        listeners foreach (_ ! incomingMessage)
      }
      case outgoingMessage: SendMessage => outgoingMessage match {
        case SendMessage(message, Some(uuid)) => sendReplyMessage(message, uuid)
        case SendMessage(message, None) => sendMessage(message)
      }
    }
  }
}
