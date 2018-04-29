package com.tradeix.concord.messages.rabbit.invoice

import com.tradeix.concord.flowmodels.invoice.InvoiceIssuanceFlowModel
import com.tradeix.concord.messages.AttachmentMessage
import com.tradeix.concord.messages.SingleIdentityMessage
import com.tradeix.concord.messages.rabbit.RabbitRequestMessage
import com.tradeix.concord.states.InvoiceState
import net.corda.core.identity.CordaX500Name
import java.math.BigDecimal
import java.time.Instant

class InvoiceIssuanceRequestMessage(
        override val correlationId: String?,
        override var tryCount: Int,
        override val externalId: String?,
        override val attachmentId: String?,
        val conductor: CordaX500Name? = CordaX500Name("TradeIX", "London", "GB"),
        val buyer: CordaX500Name?,
        val supplier: CordaX500Name?,
        val invoiceVersion: String?,
        val invoiceVersionDate: Instant?,
        val tixInvoiceVersion: Int?,
        val invoiceNumber: String?,
        val invoiceType: String?,
        val reference: String?,
        val dueDate: Instant?,
        val offerId: Int?,
        val amount: BigDecimal?,
        val totalOutstanding: BigDecimal?,
        val created: Instant?,
        val updated: Instant?,
        val expectedSettlementDate: Instant?,
        val settlementDate: Instant?,
        val mandatoryReconciliationDate: Instant?,
        val invoiceDate: Instant?,
        val status: String?,
        val rejectionReason: String?,
        val eligibleValue: BigDecimal?,
        val invoicePurchaseValue: BigDecimal?,
        val tradeDate: Instant?,
        val tradePaymentDate: Instant?,
        val invoicePayments: BigDecimal?,
        val invoiceDilutions: BigDecimal?,
        val cancelled: Boolean?,
        val closeDate: Instant?,
        val originationNetwork: String?,
        val hash: String?,
        val currency: String?,
        val siteId: String?,
        val purchaseOrderNumber: String?,
        val purchaseOrderId: String?,
        val composerProgramId: Int?
) : RabbitRequestMessage(), SingleIdentityMessage, AttachmentMessage {

    companion object {
        fun fromState(state: InvoiceState): InvoiceIssuanceRequestMessage {
            return InvoiceIssuanceRequestMessage(

                    correlationId = "",
                    tryCount = 0,
                    externalId = state.linearId.externalId.toString(),
                    attachmentId = "",
                    conductor = state.conductor.nameOrNull(),
                    buyer = state.buyer.nameOrNull(),
                    supplier = state.supplier.nameOrNull(),
                    invoiceVersion = state.invoiceVersion,
                    invoiceVersionDate = state.invoiceVersionDate,
                    tixInvoiceVersion = state.tixInvoiceVersion,
                    invoiceNumber = state.invoiceNumber,
                    invoiceType = state.invoiceType,
                    reference = state.reference,
                    dueDate = state.dueDate,
                    offerId = state.offerId,
                    amount = state.amount.toDecimal(),
                    totalOutstanding = state.totalOutstanding.toDecimal(),
                    created = state.created,
                    updated = state.updated,
                    expectedSettlementDate = state.expectedSettlementDate,
                    settlementDate = state.settlementDate,
                    mandatoryReconciliationDate = state.mandatoryReconciliationDate,
                    invoiceDate = state.invoiceDate,
                    status = state.status,
                    rejectionReason = state.rejectionReason,
                    eligibleValue = state.eligibleValue.toDecimal(),
                    invoicePurchaseValue = state.invoicePurchaseValue.toDecimal(),
                    tradeDate = state.tradeDate,
                    tradePaymentDate = state.tradePaymentDate,
                    invoicePayments = state.invoicePayments.toDecimal(),
                    invoiceDilutions = state.invoiceDilutions.toDecimal(),
                    cancelled = state.cancelled,
                    closeDate = state.closeDate,
                    originationNetwork = state.originationNetwork,
                    hash = state.hash,
                    currency = state.currency.toString(),
                    siteId = state.siteId,
                    purchaseOrderNumber = state.purchaseOrderNumber,
                    purchaseOrderId = state.purchaseOrderId,
                    composerProgramId = state.composerProgramId
            )
        }
    }

    fun toModel() = InvoiceIssuanceFlowModel(
            externalId = externalId,
            attachmentId = attachmentId,
            conductor = conductor,
            buyer = buyer,
            supplier = supplier,
            invoiceVersion = invoiceVersion,
            invoiceVersionDate = invoiceVersionDate,
            tixInvoiceVersion = tixInvoiceVersion,
            invoiceNumber = invoiceNumber,
            invoiceType = invoiceType,
            reference = reference,
            dueDate = dueDate,
            offerId = offerId,
            amount = amount,
            totalOutstanding = totalOutstanding,
            created = created,
            updated = updated,
            expectedSettlementDate = expectedSettlementDate,
            settlementDate = settlementDate,
            mandatoryReconciliationDate = mandatoryReconciliationDate,
            invoiceDate = invoiceDate,
            status = status,
            rejectionReason = rejectionReason,
            eligibleValue = eligibleValue,
            invoicePurchaseValue = invoicePurchaseValue,
            tradeDate = tradeDate,
            tradePaymentDate = tradePaymentDate,
            invoicePayments = invoicePayments,
            invoiceDilutions = invoiceDilutions,
            cancelled = cancelled,
            closeDate = closeDate,
            originationNetwork = originationNetwork,
            hash = hash,
            currency = currency,
            siteId = siteId,
            purchaseOrderNumber = purchaseOrderNumber,
            purchaseOrderId = purchaseOrderId,
            composerProgramId = composerProgramId
    )
}