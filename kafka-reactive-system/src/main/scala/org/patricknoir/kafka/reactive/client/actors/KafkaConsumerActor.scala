package org.patricknoir.kafka.reactive.client.actors

import akka.actor.{ Props, Actor, ActorLogging }
import io.circe.Decoder
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.patricknoir.kafka.reactive.common.KafkaResponseEnvelope
import org.patricknoir.kafka.reactive.ex.ConsumerException

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import io.circe.parser._
import scala.concurrent.ExecutionContext.Implicits.global
import KafkaResponseEnvelope._

/**
 * Created by patrick on 12/07/2016.
 */
class KafkaConsumerActor(consumerSettings: Map[String, String], inboundQueue: String, pollTimeout: FiniteDuration) extends Actor with ActorLogging {

  var running = true
  log.debug(s"starting kafka consumer: ${self.path.name}")

  val consumer = new KafkaConsumer[String, String](consumerSettings)
  consumer.subscribe(List(inboundQueue))

  val loop = Future {
    while (running) {
      consumer.poll(Long.MaxValue).foreach { record =>
        val result = decode[KafkaResponseEnvelope](record.value)(respEnvelopeDecoder)
        result.foreach { envelope =>
          log.debug(s"Received message: $result")
          context.actorSelection(envelope.correlationId) ! envelope
        }
      }
    }
    consumer.close()
  }

  loop.onFailure {
    case err: Throwable =>
      log.error("Error on the Kafka Consumer", err)
      throw new ConsumerException(err)
  }

  def receive = Actor.emptyBehavior

  override def postStop() = {
    running = false
  }

}

object KafkaConsumerActor {

  def props(consumerSettings: Map[String, String], inboundQueue: String, pollTimeout: FiniteDuration) =
    Props(new KafkaConsumerActor(consumerSettings, inboundQueue, pollTimeout))
}
