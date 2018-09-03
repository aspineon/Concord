package com.tradeix.concord.flows.purchaseorder

import co.paralleluniverse.fibers.Suspendable
import com.tradeix.concord.contracts.PurchaseOrderContract
import com.tradeix.concord.contracts.PurchaseOrderContract.Companion.PURCHASE_ORDER_CONTRACT_ID
import com.tradeix.concord.exceptions.FlowValidationException
import com.tradeix.concord.flowmodels.purchaseorder.PurchaseOrderIssuanceFlowModel
import com.tradeix.concord.helpers.FlowHelper
import com.tradeix.concord.helpers.VaultHelper
import com.tradeix.concord.states.PurchaseOrderState
import com.tradeix.concord.validators.purchaseorder.PurchaseOrderIssuanceFlowModelValidator
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.crypto.SecureHash.Companion.parse

object PurchaseOrderIssuance {
    @InitiatingFlow
    @StartableByRPC
    class InitiatorFlow(private val model: PurchaseOrderIssuanceFlowModel) : FlowLogic<SignedTransaction>() {

        companion object {
            object GENERATING_TRANSACTION : Step("Generating transaction based on new trade asset.")
            object VERIFYING_TRANSACTION : Step("Verifying contracts constraints.")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGNATURES : Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGNATURES,
                    FINALISING_TRANSACTION
            )

            val EX_INVALID_HASH_FOR_ATTACHMENT = "Invalid SecureHash for the Supporting Document"
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val validator = PurchaseOrderIssuanceFlowModelValidator(model)

            if (!validator.isValid) {
                throw FlowValidationException(validationErrors = validator.validationErrors)
            }

            val notary = FlowHelper.getNotary(serviceHub)
            val buyer = FlowHelper.getPeerByLegalNameOrThrow(serviceHub, model.buyer)
            val supplier = FlowHelper.getPeerByLegalNameOrThrow(serviceHub, model.supplier)
            val conductor = FlowHelper.getPeerByLegalNameOrMe(serviceHub, null)
            if (model.attachmentId != null && !VaultHelper.isAttachmentInVault(serviceHub, model.attachmentId)) {
                throw FlowValidationException(validationErrors = arrayListOf(EX_INVALID_HASH_FOR_ATTACHMENT))
            }

            // Stage 1 - Create unsigned transaction
            progressTracker.currentStep = GENERATING_TRANSACTION
            val outputState = PurchaseOrderState(
                    linearId = model.getLinearId(),
                    owner = buyer,
                    buyer = buyer,
                    supplier = supplier,
                    conductor = conductor,
                    reference = model.reference!!,
                    amount = model.amount,
                    created = model.created!!,
                    earliestShipment = model.earliestShipment!!,
                    latestShipment = model.latestShipment!!,
                    portOfShipment = model.portOfShipment!!,
                    descriptionOfGoods = model.descriptionOfGoods!!,
                    deliveryTerms = model.deliveryTerms!!
            )

            val command = Command(
                    value = PurchaseOrderContract.Commands.Issue(),
                    signers = outputState.participants.map { it.owningKey })

            val transactionBuilder = TransactionBuilder(notary)
                    .addOutputState(outputState, PURCHASE_ORDER_CONTRACT_ID)
                    .addCommand(command)

            if (model.attachmentId != null) {
                transactionBuilder.addAttachment(parse(model.attachmentId))
            }

            // Stage 2 - Verify transaction
            progressTracker.currentStep = VERIFYING_TRANSACTION
            transactionBuilder.verify(serviceHub)

            // Stage 3 - Sign the transaction
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partiallySignedTransaction = serviceHub.signInitialTransaction(transactionBuilder)

            // Stage 4 - Gather counterparty signatures
            progressTracker.currentStep = GATHERING_SIGNATURES
            val requiredSignatureFlowSessions = listOf(
                    outputState.owner,
                    outputState.buyer,
                    outputState.supplier,
                    outputState.conductor)
                    .filter { !serviceHub.myInfo.legalIdentities.contains(it) }
                    .distinct()
                    .map { initiateFlow(serviceHub.identityService.requireWellKnownPartyFromAnonymous(it)) }

            // TODO : Move this into FlowHelper ^

            val fullySignedTransaction = subFlow(CollectSignaturesFlow(
                    partiallySignedTransaction,
                    requiredSignatureFlowSessions,
                    GATHERING_SIGNATURES.childProgressTracker()))

            // Stage 5 - Finalize transaction
            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(FinalityFlow(
                    transaction = fullySignedTransaction,
                    progressTracker = FINALISING_TRANSACTION.childProgressTracker()))
        }

    }

    @InitiatedBy(InitiatorFlow::class)
    class AcceptorFlow(val otherPartyFlow: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(otherPartyFlow) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be a purchase order transaction." using (output is PurchaseOrderState)
                }
            }

            return subFlow(signTransactionFlow)
        }
    }
}