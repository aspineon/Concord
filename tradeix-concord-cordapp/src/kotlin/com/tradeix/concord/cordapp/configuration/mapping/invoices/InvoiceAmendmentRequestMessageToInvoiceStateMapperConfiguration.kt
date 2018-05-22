package com.tradeix.concord.cordapp.configuration.mapping.invoices

import com.tradeix.concord.shared.data.VaultRepository
import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.extensions.*
import com.tradeix.concord.shared.mapper.MapperConfiguration
import com.tradeix.concord.shared.messages.invoices.InvoiceAmendmentRequestMessage
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import java.util.*

class InvoiceAmendmentRequestMessageToInvoiceStateMapperConfiguration
    : MapperConfiguration<InvoiceAmendmentRequestMessage, InvoiceState>() {

    override fun map(source: InvoiceAmendmentRequestMessage, serviceHub: ServiceHub): InvoiceState {

        val repository = VaultRepository.fromServiceHub<InvoiceState>(serviceHub)

        val inputState = repository
                .findByExternalId(source.externalId!!, Vault.StateStatus.UNCONSUMED)
                .singleOrNull()

        if (inputState == null) {
            throw FlowException("InvoiceState with externalId '${source.externalId}' does not exist.")
        } else {

            val buyer = serviceHub.networkMapCache.getPartyFromLegalNameOrNull(
                    CordaX500Name.tryParse(source.buyer)
            )

            val supplier = serviceHub.networkMapCache.getPartyFromLegalNameOrMe(
                    serviceHub,
                    CordaX500Name.tryParse(source.supplier)
            )

            val conductor = serviceHub.networkMapCache.getPartyFromLegalNameOrDefault(
                    CordaX500Name.tryParse(source.conductor),
                    CordaX500Name.defaultConductor
            )

            return inputState.state.data.copy(
                    owner = supplier,
                    buyer = buyer,
                    supplier = supplier,
                    conductor = conductor,
                    invoiceVersion = source.invoiceVersion!!,
                    invoiceVersionDate = source.invoiceVersionDate!!,
                    tixInvoiceVersion = source.tixInvoiceVersion!!,
                    invoiceNumber = source.invoiceNumber!!,
                    invoiceType = source.invoiceType!!,
                    reference = source.reference!!,
                    dueDate = source.dueDate!!,
                    offerId = source.offerId,
                    amount = Amount.fromValueAndCurrency(source.amount!!, source.currency!!),
                    totalOutstanding = Amount.fromValueAndCurrency(source.totalOutstanding!!, source.currency!!),
                    created = source.created!!,
                    updated = source.updated!!,
                    expectedSettlementDate = source.expectedSettlementDate!!,
                    settlementDate = source.settlementDate,
                    mandatoryReconciliationDate = source.mandatoryReconciliationDate,
                    invoiceDate = source.invoiceDate!!,
                    status = source.status!!,
                    rejectionReason = source.rejectionReason!!,
                    eligibleValue = Amount.fromValueAndCurrency(source.eligibleValue!!, source.currency!!),
                    invoicePurchaseValue = Amount.fromValueAndCurrency(source.invoicePurchaseValue!!, source.currency!!),
                    tradeDate = source.tradeDate,
                    tradePaymentDate = source.tradePaymentDate,
                    invoicePayments = Amount.fromValueAndCurrency(source.invoicePayments!!, source.currency!!),
                    invoiceDilutions = Amount.fromValueAndCurrency(source.invoiceDilutions!!, source.currency!!),
                    cancelled = source.cancelled!!,
                    closeDate = source.closeDate,
                    originationNetwork = source.originationNetwork!!,
                    currency = Currency.getInstance(source.currency!!),
                    siteId = source.siteId!!,
                    purchaseOrderNumber = source.purchaseOrderNumber!!,
                    purchaseOrderId = source.purchaseOrderId!!,
                    composerProgramId = source.composerProgramId
            )
        }
    }
}