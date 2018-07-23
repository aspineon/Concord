package com.tradeix.concord.tests.unit.flows.fundingresponse

import com.tradeix.concord.shared.cordapp.flows.CollectSignaturesResponderFlow
import com.tradeix.concord.shared.cordapp.flows.ObserveTransactionResponderFlow
import com.tradeix.concord.shared.mockdata.MockFundingResponses.FUNDING_RESPONSE_ISSUANCE_REQUEST_MESSAGE
import com.tradeix.concord.shared.mockdata.MockInvoices
import com.tradeix.concord.shared.mockdata.ParticipantType
import com.tradeix.concord.tests.unit.flows.FlowTest
import com.tradeix.concord.tests.unit.flows.invoices.InvoiceFlows
import net.corda.testing.node.StartedMockNode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FundingResponseIssuanceFlowTests : FlowTest() {

    override fun configureNode(node: StartedMockNode, type: ParticipantType) {
        if (type == ParticipantType.BUYER) {
            node.registerInitiatedFlow(CollectSignaturesResponderFlow::class.java)
        }

        if (type == ParticipantType.SUPPLIER) {
            node.registerInitiatedFlow(CollectSignaturesResponderFlow::class.java)
        }

        if (type == ParticipantType.FUNDER) {
            node.registerInitiatedFlow(CollectSignaturesResponderFlow::class.java)
            node.registerInitiatedFlow(ObserveTransactionResponderFlow::class.java)
        }
    }

    override fun initialize() {
        InvoiceFlows.issue(
                network = network,
                initiator = supplier1.node,
                message = MockInvoices.createMockInvoices(
                        count = 3,
                        buyer = buyer1.name,
                        supplier = supplier1.name,
                        observers = listOf(funder1.name)
                )
        )
    }

    @Test
    fun `Funding response issuance flow must be signed by the initiator`() {
        val transaction = FundingResponseFlows.issue(
                network = network,
                initiator = funder1.node,
                message = FUNDING_RESPONSE_ISSUANCE_REQUEST_MESSAGE.copy(
                        invoiceExternalIds = listOf("INVOICE_1", "INVOICE_2", "INVOICE_3")
                )
        )

        transaction.verifySignaturesExcept(supplier1.publicKey)
    }

    @Test
    fun `Funding response issuance flow must be signed by the acceptor`() {
        val transaction = FundingResponseFlows.issue(
                network = network,
                initiator = funder1.node,
                message = FUNDING_RESPONSE_ISSUANCE_REQUEST_MESSAGE.copy(
                        invoiceExternalIds = listOf("INVOICE_1", "INVOICE_2", "INVOICE_3")
                )
        )

        transaction.verifySignaturesExcept(funder1.publicKey)
    }

    @Test
    fun `Funding response issuance flow records a transaction in all counter-party vaults`() {
        val transaction = FundingResponseFlows.issue(
                network = network,
                initiator = funder1.node,
                message = FUNDING_RESPONSE_ISSUANCE_REQUEST_MESSAGE.copy(
                        invoiceExternalIds = listOf("INVOICE_1", "INVOICE_2", "INVOICE_3")
                )
        )

        listOf(supplier1.node, funder1.node).forEach {
            assertEquals(transaction, it.services.validatedTransactions.getTransaction(transaction.id))
        }
    }

    @Test
    fun `Funding response issuance flow has zero inputs and only one output`() {
        val transaction = FundingResponseFlows.issue(
                network = network,
                initiator = funder1.node,
                message = FUNDING_RESPONSE_ISSUANCE_REQUEST_MESSAGE.copy(
                        invoiceExternalIds = listOf("INVOICE_1", "INVOICE_2", "INVOICE_3")
                )
        )

        listOf(supplier1.node, funder1.node).forEach {
            val recordedTransaction = it.services.validatedTransactions.getTransaction(transaction.id) ?: fail()
            assertEquals(0, recordedTransaction.tx.inputs.size)
            assertEquals(1, recordedTransaction.tx.outputs.size)
        }
    }
}