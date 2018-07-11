package com.tradeix.concord.shared.cordapp.mapping.invoices

import com.tradeix.concord.shared.domain.states.InvoiceState
import com.tradeix.concord.shared.mapper.Mapper
import com.tradeix.concord.shared.messages.CancellationRequestMessage
import com.tradeix.concord.shared.services.VaultService
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowException
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.Vault

class InvoiceCancellationRequestMapper(private val serviceHub: ServiceHub)
    : Mapper<CancellationRequestMessage, StateAndRef<InvoiceState>>() {

    override fun map(source: CancellationRequestMessage): StateAndRef<InvoiceState> {

        val vaultService = VaultService.fromServiceHub<InvoiceState>(serviceHub)

        val inputState = vaultService
                .findByExternalId(source.externalId!!, status = Vault.StateStatus.UNCONSUMED)
                .singleOrNull()

        return inputState ?: throw FlowException("InvoiceState with externalId '${source.externalId}' does not exist.")
    }
}