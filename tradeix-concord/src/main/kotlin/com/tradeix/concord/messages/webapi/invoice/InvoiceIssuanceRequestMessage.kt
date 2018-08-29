package com.tradeix.concord.messages.webapi.invoice

import com.tradeix.concord.flowmodels.invoice.InvoiceIssuanceFlowModel
import com.tradeix.concord.messages.AttachmentMessage
import com.tradeix.concord.messages.SingleIdentityMessage
import net.corda.core.identity.CordaX500Name
import java.math.BigDecimal
import java.time.Instant

class InvoiceIssuanceRequestMessage(
        override val externalId: String?,
        override val attachmentId: String?,
        val conductor: CordaX500Name?,
        val buyer: CordaX500Name?,
        val supplier: CordaX500Name?,
        val funder: CordaX500Name?,
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
) : SingleIdentityMessage, AttachmentMessage {
    fun toModel() = InvoiceIssuanceFlowModel(
            externalId,
            attachmentId,
            conductor,
            buyer,
            supplier,
            funder,
            invoiceVersion,
            invoiceVersionDate,
            tixInvoiceVersion,
            invoiceNumber,
            invoiceType,
            reference,
            dueDate,
            offerId,
            amount,
            totalOutstanding,
            created,
            updated,
            expectedSettlementDate,
            settlementDate,
            mandatoryReconciliationDate,
            invoiceDate,
            status,
            rejectionReason,
            eligibleValue,
            invoicePurchaseValue,
            tradeDate,
            tradePaymentDate,
            invoicePayments,
            invoiceDilutions,
            cancelled,
            closeDate,
            originationNetwork,
            hash,
            currency,
            siteId,
            purchaseOrderNumber,
            purchaseOrderId,
            composerProgramId
    )
}