package com.tradeix.concord.contracts

import com.tradeix.concord.models.PurchaseOrder
import com.tradeix.concord.states.PurchaseOrderState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.finance.POUNDS
import net.corda.testing.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class PurchaseOrderIssuanceContractTests {
    @Before
    fun setup() {
        setCordappPackages("com.tradeix.concord.contracts")
    }

    @After
    fun tearDown() {
        unsetCordappPackages()
    }

    @Test
    fun `Purchase Order Issuance transaction must include Issue command`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                fails()
                command(ALICE_PUBKEY, BOB_PUBKEY, BIG_CORP_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                verifies()
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction must have no inputs`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                input(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                command(ALICE_PUBKEY, BOB_PUBKEY, BIG_CORP_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("No inputs should be consumed when issuing a purchase order.")
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction must have one output`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                command(ALICE_PUBKEY, BOB_PUBKEY, BIG_CORP_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction must be signed by the buyer`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                command(BOB_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction must be signed by the supplier`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                command(ALICE_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction must be signed by conductor`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = ALICE,
                            buyer = BOB,
                            supplier = ALICE,
                            conductor = BIG_CORP)
                }
                command(BIG_CORP_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `Purchase Order Issuance transaction cannot have buyer and supplier as the same entity`() {
        val linearId = UniqueIdentifier(id = UUID.fromString("00000000-0000-4000-0000-000000000000"))
        val purchaseOrder = PurchaseOrder(1.POUNDS)
        ledger {
            transaction {
                output(PurchaseOrderContract.PURCHASE_ORDER_CONTRACT_ID) {
                    PurchaseOrderState(
                            linearId = linearId,
                            purchaseOrder = purchaseOrder,
                            owner = BOB,
                            buyer = BOB,
                            supplier = BOB,
                            conductor = BIG_CORP)
                }
                command(ALICE_PUBKEY, BOB_PUBKEY, BIG_CORP_PUBKEY) {
                    PurchaseOrderContract.Commands.Issue()
                }
                `fails with`("The buyer and the supplier cannot be the same entity.")
            }
        }
    }
}
