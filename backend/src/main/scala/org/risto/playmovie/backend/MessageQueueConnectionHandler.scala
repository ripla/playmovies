package org.risto.playmovie.backend

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import com.rabbitmq.client.{Channel, ConnectionFactory}
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


/**
 * @author Risto Yrjänä
 */
object MessageQueueConnectionHandler {

  var connectionFactory = new ConnectionFactory()

  def createQueryChannel(): Try[Channel] = {
    val conf = ConfigFactory.load().withFallback(ConfigFactory.empty().withValue("CLOUDAMQP_URL", ConfigValueFactory.fromAnyRef("amqp://guest:guest@localhost")))
    val uri = conf.getString("CLOUDAMQP_URL")

    connectionFactory.setUri(uri)
    Try(connectionFactory.newConnection()) map (connection => Try(connection.createChannel())) flatten
  }

  case class Connection(c: Channel)

  case object GetConnection

}

/**
 * @author Risto Yrjänä
 */
class MessageQueueConnectionHandler(connectionUser: ActorRef) extends Actor with ActorLogging {

  var cancellable: Option[Cancellable] = None

  override def preStart(): Unit = tryConnection()

  def tryConnection(): Unit = {
    MessageQueueConnectionHandler.createQueryChannel() match {
      case Success(channel) => {
        connectionUser ! MessageQueueConnectionHandler.Connection(channel)
        context.stop(self)
      }
      case Failure(exception) => {
        log.error(exception, "Failed to retrieve MQ connection")
        import context.dispatcher
        cancellable = Some(context.system.scheduler.scheduleOnce(500 milliseconds, self, MessageQueueConnectionHandler.GetConnection))
      }
    }
  }

  override def postStop(): Unit = cancellable foreach (_.cancel())

  def receive = {
    case MessageQueueConnectionHandler.GetConnection => tryConnection()
  }
}
