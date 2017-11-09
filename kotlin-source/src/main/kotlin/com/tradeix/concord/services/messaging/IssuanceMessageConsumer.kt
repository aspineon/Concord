package com.tradeix.concord.services.messaging

import com.google.gson.Gson
import com.rabbitmq.client.*
import com.tradeix.concord.interfaces.IQueueDeadLetterProducer
import com.tradeix.concord.messages.Message
import com.tradeix.concord.messages.TradeAssetIssuanceRequestMessage
import com.tradeix.concord.messages.TransactionResponseMessage
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import java.nio.charset.Charset

class IssuanceMessageConsumer(
        val services: CordaRPCOps,
        private val channel: Channel,
        private val deadLetterProducer: IQueueDeadLetterProducer<Message>,
        private val maxRetryCount: Int,
        private val responder: RabbitMqProducer<TransactionResponseMessage>
) : Consumer {

    override fun handleRecoverOk(consumerTag: String?) {
        println("IssuanceMessageConsumer: handleRecoverOk for consumer tag: $consumerTag")
    }

    override fun handleConsumeOk(consumerTag: String?) {
        println("IssuanceMessageConsumer: handleConsumeOk for consumer tag: $consumerTag")
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        println("IssuanceMessageConsumer: handleShutdownSignal for consumer tag: $consumerTag")
        println(sig)
    }

    override fun handleCancel(consumerTag: String?) {
        println("IssuanceMessageConsumer: handleCancel for consumer tag: $consumerTag")
    }

    override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?) {
        val deliveryTag = envelope?.deliveryTag

        // Handler logic here
        val messageBody = body?.toString(Charset.defaultCharset())
        println("Received message $messageBody")
        val serializer = Gson()

        var requestMessage = TradeAssetIssuanceRequestMessage(
                correlationId = null,
                tryCount = 1,
                externalId = null,
                buyer = null,
                supplier = null,
                conductor = CordaX500Name("TradeIX", "London", "GB"),
                status = null,
                value = null,
                currency = null,
                attachmentId = null)

        try {
            println("Received message in IssuanceMessageConsumer - about to ack then process.")
            requestMessage = serializer.fromJson(messageBody, TradeAssetIssuanceRequestMessage::class.java)
            println("Successfully processed IssuanceRequest - responding back to client")
            val response = TransactionResponseMessage("1", 0, "1")
            responder.publish(response)
        } catch (ex: Throwable) {
            if (requestMessage.tryCount < maxRetryCount) {
                println("Exception handled in IssuanceMessageConsumer, writing to dlq")
                deadLetterProducer.publish(requestMessage)
            } else {
                println("Exception handled in IssuanceMessageConsumer, writing to dlq fatally")
                deadLetterProducer.publish(requestMessage, isFatal = true)
            }
        } finally {
            channel.basicAck(deliveryTag!!, false)
        }
    }

    override fun handleCancelOk(consumerTag: String?) {
        println("IssuanceMessageConsumer: handleCancelOk for consumer tag: $consumerTag")
    }
}