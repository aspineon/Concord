package com.tradeix.concord.flows

import com.tradeix.concord.exceptions.RequestValidationException
import com.tradeix.concord.messages.TradeAssetIssuanceRequestMessage
import groovy.util.GroovyTestCase.assertEquals
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import net.corda.testing.*
import net.corda.core.identity.Party
import net.corda.core.utilities.getOrThrow
import java.math.BigDecimal
import kotlin.test.assertFailsWith
import kotlin.test.fail

class TradeAssetIssuanceFlowTests {
    lateinit var network: MockNetwork
    lateinit var mockBuyerNode: StartedNode<MockNetwork.MockNode>
    lateinit var mockSupplierNode: StartedNode<MockNetwork.MockNode>
    lateinit var mockConductorNode: StartedNode<MockNetwork.MockNode>

    lateinit var mockBuyer: Party
    lateinit var mockSupplier: Party
    lateinit var mockConductor: Party

    @Before
    fun setup() {
        setCordappPackages("com.tradeix.concord.contracts")
        network = MockNetwork()
        val nodes = network.createSomeNodes(3)
        mockBuyerNode = nodes.partyNodes[0]
        mockSupplierNode = nodes.partyNodes[1]
        mockConductorNode = nodes.partyNodes[2]

        mockBuyer = mockBuyerNode.info.chooseIdentity()
        mockSupplier = mockSupplierNode.info.chooseIdentity()
        mockConductor = mockConductorNode.info.chooseIdentity()

        nodes.partyNodes.forEach { it.registerInitiatedFlow(TradeAssetIssuance.Acceptor::class.java) }

        network.runNetwork()
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
        network.stopNodes()
    }

    @Test
    fun `Absence of supplier in message should result in error`() {
        val message = TradeAssetIssuanceRequestMessage(
                buyer = mockBuyer.name,
                supplier = null,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        )

        assertFailsWith<RequestValidationException>("Request validation failed") {
            val flow = TradeAssetIssuance.InitiatorFlow(message)

            val future = mockBuyerNode.services.startFlow(flow).resultFuture
            network.runNetwork()

            future.getOrThrow()
        }

        assert(!message.isValid)
        assert(message.getValidationErrors().size == 1)
        assert(message.getValidationErrors().contains("Supplier is required for an issuance transaction"))
    }

    @Test
    fun `Absence of asset ID in message should result in error`() {
        val message = TradeAssetIssuanceRequestMessage(
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                assetId = null,
                value = BigDecimal.ONE,
                currency = "GBP"
        )

        assertFailsWith<RequestValidationException>("Request validation failed") {
            val flow = TradeAssetIssuance.InitiatorFlow(message)

            val future = mockBuyerNode.services.startFlow(flow).resultFuture
            network.runNetwork()

            future.getOrThrow()
        }

        assert(!message.isValid)
        assert(message.getValidationErrors().size == 1)
        assert(message.getValidationErrors().contains("Asset ID is required for an issuance transaction"))
    }

    @Test
    fun `Absence of currency in message should result in error`() {
        val message = TradeAssetIssuanceRequestMessage(
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = null
        )

        assertFailsWith<RequestValidationException>("Request validation failed") {
            val flow = TradeAssetIssuance.InitiatorFlow(message)

            val future = mockBuyerNode.services.startFlow(flow).resultFuture
            network.runNetwork()

            future.getOrThrow()
        }

        assert(!message.isValid)
        assert(message.getValidationErrors().size == 1)
        assert(message.getValidationErrors().contains("Currency is required for an issuance transaction"))
    }

    @Test
    fun `Absence of value in message should result in error`() {
        val message = TradeAssetIssuanceRequestMessage(
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                assetId = "MOCK_ASSET",
                value = null,
                currency = "GBP"
        )

        assertFailsWith<RequestValidationException>("Request validation failed") {
            val flow = TradeAssetIssuance.InitiatorFlow(message)

            val future = mockBuyerNode.services.startFlow(flow).resultFuture
            network.runNetwork()

            future.getOrThrow()
        }

        assert(!message.isValid)
        assert(message.getValidationErrors().size == 1)
        assert(message.getValidationErrors().contains("Value is required for an issuance transaction"))
    }

    @Test
    fun `Negative value in message should result in error`() {
        val message = TradeAssetIssuanceRequestMessage(
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE.negate(),
                currency = "GBP"
        )

        assertFailsWith<RequestValidationException>("Request validation failed") {
            val flow = TradeAssetIssuance.InitiatorFlow(message)

            val future = mockBuyerNode.services.startFlow(flow).resultFuture
            network.runNetwork()

            future.getOrThrow()
        }

        assert(!message.isValid)
        assert(message.getValidationErrors().size == 1)
        assert(message.getValidationErrors().contains("Value cannot be negative for an issuance transaction"))
    }

    @Test
    fun `Buyer initiated SignedTransaction returned by the flow is signed by the initiator`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = null,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))

        val future = mockBuyerNode.services.startFlow(flow).resultFuture
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(mockConductor.owningKey, mockSupplier.owningKey)
    }

    @Test
    fun `Buyer initiated SignedTransaction returned by the flow is signed by the acceptor`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = null,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))

        val future = mockBuyerNode.services.startFlow(flow).resultFuture
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(mockBuyer.owningKey)
    }

    @Test
    fun `Conductor initiated SignedTransaction returned by the flow is signed by the initiator`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))

        val future = mockConductorNode.services.startFlow(flow).resultFuture
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(mockConductor.owningKey, mockSupplier.owningKey)
    }

    @Test
    fun `Conductor initiated SignedTransaction returned by the flow is signed by the acceptor`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = mockBuyer.name,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))

        val future = mockConductorNode.services.startFlow(flow).resultFuture
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(mockBuyer.owningKey)
    }

    @Test
    fun `Flow records a transaction in all counter-party vaults`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = null,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))
        val future = mockBuyerNode.services.startFlow(flow).resultFuture
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both vaults.
        for (node in listOf(mockBuyerNode, mockSupplierNode, mockConductorNode)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `Recorded transaction has no inputs and a single output`() {
        val flow = TradeAssetIssuance.InitiatorFlow(TradeAssetIssuanceRequestMessage(
                linearId = UUID.fromString("00000000-0000-4000-0000-000000000000"),
                buyer = null,
                supplier = mockSupplier.name,
                conductor = mockConductor.name,
                assetId = "MOCK_ASSET",
                value = BigDecimal.ONE,
                currency = "GBP"
        ))
        val future = mockBuyerNode.services.startFlow(flow).resultFuture
        network.runNetwork()
        val signedTx = future.getOrThrow()

        for (node in listOf(mockBuyerNode, mockSupplierNode, mockConductorNode)) {
            val recordedTx = node.services.validatedTransactions.getTransaction(signedTx.id) ?: fail()
            assert(recordedTx.inputs.isEmpty())
            assert(recordedTx.tx.outputs.size == 1)
        }
    }
}
