package com.tradeix.concord.services.messaging.consumers

import com.google.gson.Gson
import com.rabbitmq.client.*
import com.tradeix.concord.exceptions.FlowValidationException
import com.tradeix.concord.flows.purchaseorder.PurchaseOrderIssuance
import com.tradeix.concord.interfaces.IQueueDeadLetterProducer
import com.tradeix.concord.messages.rabbit.RabbitMessage
import com.tradeix.concord.messages.rabbit.purchaseorder.PurchaseOrderIssuanceRequestMessage
import com.tradeix.concord.messages.rabbit.purchaseorder.PurchaseOrderResponseMessage
import com.tradeix.concord.services.messaging.RabbitMqProducer
import com.tradeix.concord.validators.RabbitRequestMessageValidator
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import java.nio.charset.Charset

class PurchaseOrderIssuanceMessageConsumer(
        val services: CordaRPCOps,
        private val channel: Channel,
        private val deadLetterProducer: IQueueDeadLetterProducer<RabbitMessage>,
        private val maxRetryCount: Int,
        private val responder: RabbitMqProducer<PurchaseOrderResponseMessage>,
        private val serializer: Gson
) : Consumer {

    companion object {
        protected val log: Logger = loggerFor<PurchaseOrderIssuanceMessageConsumer>()
    }

    override fun handleRecoverOk(consumerTag: String?) {
        println("PurchaseOrderIssuanceMessageConsumer: handleRecoverOk for consumer tag: $consumerTag")
    }

    override fun handleConsumeOk(consumerTag: String?) {
        println("PurchaseOrderIssuanceMessageConsumer: handleConsumeOk for consumer tag: $consumerTag")
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        println("PurchaseOrderIssuanceMessageConsumer: handleShutdownSignal for consumer tag: $consumerTag")
        println(sig)
    }

    override fun handleCancel(consumerTag: String?) {
        println("PurchaseOrderIssuanceMessageConsumer: handleCancel for consumer tag: $consumerTag")
    }

    override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?) {
        // val deliveryTag = envelope?.deliveryTag

        // Handler logic here
        val messageBody = body?.toString(Charset.defaultCharset())
        println("Received message $messageBody")

        var requestMessage = PurchaseOrderIssuanceRequestMessage(
                correlationId = null,
                created = null,
                deliveryTerms = null,
                descriptionOfGoods = null,
                earliestShipment = null,
                latestShipment = null,
                portOfShipment = null,
                reference = null,
                tryCount = 0,
                externalId = null,
                buyer = null,
                supplier = null,
                value = null,
                currency = null,
                attachmentId = null)

        try {
            requestMessage = serializer.fromJson(messageBody, PurchaseOrderIssuanceRequestMessage::class.java)
            println("Received message with id ${requestMessage.correlationId} in PurchaseOrderIssuanceMessageConsumer - about to process.")
            log.info("Received message with id ${requestMessage.correlationId} in PurchaseOrderIssuanceMessageConsumer - about to process.")

            try {
                val validator = RabbitRequestMessageValidator(requestMessage)

                if (!validator.isValid) {
                    throw FlowValidationException(validationErrors = validator.validationErrors)
                }

                val flowHandle = services.startTrackedFlow(PurchaseOrderIssuance::InitiatorFlow, requestMessage.toModel())
                flowHandle.progress.subscribe { println(">> $it") }
                val result = flowHandle.returnValue.getOrThrow()


                println("Successfully processed IssuanceRequest - responding back to client")
                val response = PurchaseOrderResponseMessage(
                        correlationId = requestMessage.correlationId!!,
                        transactionId = result.id.toString(),
                        errorMessages = null,
                        externalIds = listOf(requestMessage.externalId!!),
                        success = true,
                        bidUniqueId = null
                )
                responder.publish(response)
                log.info("Successfully processed IssuanceRequest - responded back to client")
            } catch (ex: Throwable) {
                log.error("Failed to process the message ${ex.message}, Returning a error response")
                return when (ex) {
                    is FlowValidationException -> {
                        println("Flow validation exception occurred, sending failed response")
                        val response = PurchaseOrderResponseMessage(
                                correlationId = requestMessage.correlationId!!,
                                transactionId = null,
                                errorMessages = ex.validationErrors,
                                externalIds = listOf(requestMessage.externalId!!),
                                success = false,
                                bidUniqueId = null
                        )

                        responder.publish(response)
                    }
                    else -> {
                        val response = PurchaseOrderResponseMessage(
                                correlationId = requestMessage.correlationId!!,
                                transactionId = null,
                                errorMessages = listOf(ex.message!!),
                                externalIds = listOf(requestMessage.externalId!!),
                                success = false,
                                bidUniqueId = null
                        )

                        responder.publish(response)
                    }
                }

            }
        } catch (ex: Throwable) {
            requestMessage.tryCount++
            if (requestMessage.tryCount < maxRetryCount) {
                println("Exception handled in PurchaseOrderIssuanceMessageConsumer, writing to dlq")
                log.error("Exception handled in PurchaseOrderIssuanceMessageConsumer, writing to dlq")

                deadLetterProducer.publish(requestMessage, false)
            } else {
                println("Exception handled in PurchaseOrderIssuanceMessageConsumer, writing to dlq fatally")
                log.error("Exception handled in PurchaseOrderIssuanceMessageConsumer, writing to dlq fatally")
                deadLetterProducer.publish(requestMessage, true)
            }
        }
    }

    override fun handleCancelOk(consumerTag: String?) {
        println("PurchaseOrderIssuanceMessageConsumer: handleCancelOk for consumer tag: $consumerTag")
    }
}