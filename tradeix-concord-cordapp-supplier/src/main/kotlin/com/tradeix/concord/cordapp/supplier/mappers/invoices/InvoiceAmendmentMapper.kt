package com.tradeix.concord.cordapp.supplier.mappers.invoices

import com.tradeix.concord.cordapp.supplier.messages.invoices.InvoiceRequestMessage
import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.extensions.fromValueAndCurrency
import com.tradeix.concord.shared.extensions.tryParse
import com.tradeix.concord.shared.mapper.InputAndOutput
import com.tradeix.concord.shared.mapper.Mapper
import com.tradeix.concord.shared.models.Participant
import com.tradeix.concord.shared.services.IdentityService
import com.tradeix.concord.shared.services.VaultService
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowException
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault
import java.time.LocalDateTime

class InvoiceAmendmentMapper(private val serviceHub: ServiceHub)
    : Mapper<InvoiceRequestMessage, InputAndOutput<InvoiceState>>() {

    override fun map(source: InvoiceRequestMessage): InputAndOutput<InvoiceState> {

        val vaultService = VaultService.fromServiceHub<InvoiceState>(serviceHub)
        val identityService = IdentityService(serviceHub)

        val inputState = vaultService
                .findByExternalId(source.externalId!!, status = Vault.StateStatus.UNCONSUMED)
                .singleOrNull()

        val count = vaultService.getCount(source.externalId)

        if (inputState == null) {
            throw FlowException("InvoiceState with externalId '${source.externalId}' does not exist.")
        } else {

            val buyer = identityService.getPartyFromLegalNameOrNull(CordaX500Name.tryParse(source.buyer))
            val supplier = identityService.getPartyFromLegalNameOrMe(CordaX500Name.tryParse(source.supplier))

            val outputState = inputState.state.data.copy(
                    owner = supplier,
                    buyer = Participant(buyer, source.buyerCompanyReference),
                    supplier = Participant(supplier, source.supplierCompanyReference),
                    invoiceNumber = source.invoiceNumber!!,
                    invoiceVersion = "${count + 1}.0",
                    submitted = LocalDateTime.now(),
                    reference = source.reference!!,
                    dueDate = source.dueDate!!,
                    amount = Amount.fromValueAndCurrency(source.amount!!, source.currency!!),
                    totalOutstanding = Amount.fromValueAndCurrency(source.totalOutstanding!!, source.currency),
                    settlementDate = source.settlementDate!!,
                    invoiceDate = source.invoiceDate!!,
                    invoicePayments = Amount.fromValueAndCurrency(source.invoicePayments!!, source.currency),
                    invoiceDilutions = Amount.fromValueAndCurrency(source.invoiceDilutions!!, source.currency),
                    originationNetwork = source.originationNetwork!!,
                    siteId = source.siteId!!,
                    tradeDate = source.tradeDate,
                    tradePaymentDate = source.tradePaymentDate
            )

            return InputAndOutput(inputState, outputState)
        }
    }
}