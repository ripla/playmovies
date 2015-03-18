package org.risto.playmovie.backend

import akka.actor._
import akka.event.LoggingReceive
import com.rabbitmq.client._
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.risto.playmovie.backend.MQAdapterProtocol.{Message, Register, SendMessage, UnRegister}
import org.risto.playmovie.common.Global.Implicits._

import scala.collection.mutable.ListBuffer
import scala.util.Try

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

  val queue: String = "playQueryQueue"

  def createQueryChannel(): Try[Channel] = {
    val conf = ConfigFactory.load().withFallback(ConfigFactory.empty().withValue("CLOUDAMQP_URL", ConfigValueFactory.fromAnyRef("amqp://guest:guest@localhost")))
    val uri = conf.getString("CLOUDAMQP_URL")

    val factory = new ConnectionFactory()
    factory.setUri(uri)
    Try(factory.newConnection()) map (connection => Try(connection.createChannel())) flatten
  }

  case class Connection(c: Channel)

}

/**
 * Generic Actor that acts as a bridge between an AMQP and the Actor system. Handles messages in both directions, as well as replies.
 *
 * @author Risto Yrjänä
 */
class MessageQueueAdapter extends Actor with ActorLogging with Stash {

  /**
   * Common message handling (listener handling) for all states
   */
  val registrationHandling: Receive = LoggingReceive {
    case Register(actor) => {
      messageListeners += actor
      log.debug(s"Registered new listener, current listeners: $messageListeners")
    }
    case UnRegister(actor) => messageListeners -= actor
  }
  /**
   * While waiting for connection, we stash all messages. Once we get the connection, we unload the stash and switch
   * to normal behaviour. This doesn't handle losing the connection
   */
  val waitingForConnection: Receive = registrationHandling orElse LoggingReceive {
    case MessageQueueConnectionHandler.Connection(c) => {
      //TODO should we try to remove existing consumer bindings?
      channel = c
      channel.queueDeclare(MessageQueueAdapter.queue, true, false, false, null)
      channel.basicConsume(MessageQueueAdapter.queue, true, consumer)

      unstashAll()
      context.become(handlingMessages)
    }

    case otherMessage => stash()
  }
  val handlingMessages: Receive = registrationHandling orElse
    LoggingReceive {

      case incomingMessage: Message => {
        log.debug(s"Sending $incomingMessage to $messageListeners")
        messageListeners foreach (_ ! incomingMessage)
      }
      case outgoingMessage: SendMessage => outgoingMessage match {
        case SendMessage(message, Some(uuid)) => sendReplyMessage(message, uuid)
        case SendMessage(message, None) => sendMessage(message)
      }
    }
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
  var messageListeners: ListBuffer[ActorRef] = ListBuffer.empty
  var idReplyToQueue: Map[String, String] = Map.empty
  var channel: Channel = _

  override def preStart(): Unit = {
    log.debug(s"${this.getClass.getSimpleName} prestart")
    context.actorOf(Props(classOf[MessageQueueConnectionHandler], context.self))
  }

  override def postStop(): Unit = {
    channel.close()
  }

  def sendReplyMessage(message: String, uuid: String): Unit = {
    channel.basicPublish("", getReplyQueue(uuid), MessageProperties.PERSISTENT_TEXT_PLAIN.builder().correlationId(uuid).build(), message.getBytes())
  }

  def getReplyQueue(id: String): String = idReplyToQueue.get(id) match {
    case Some(replyToQueue) => replyToQueue
    case None => throw new IllegalStateException(s"No replyToQueue found for message id $id")
  }

  def sendMessage(message: String): Unit = {
    channel.basicPublish("", MessageQueueAdapter.queue, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes())
  }

  def receive = waitingForConnection
}
