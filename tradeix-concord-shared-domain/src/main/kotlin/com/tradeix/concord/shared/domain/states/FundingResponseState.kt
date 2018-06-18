package com.tradeix.concord.shared.domain.states

import com.tradeix.concord.shared.domain.enumerations.FundingResponseStatus
import com.tradeix.concord.shared.domain.mapping.FundingResponseSchemaV1Mapper
import com.tradeix.concord.shared.domain.schemas.FundingResponseSchemaV1
import net.corda.core.contracts.Amount
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

data class FundingResponseState(
        override val linearId: UniqueIdentifier,
        val fundingRequestId: UniqueIdentifier? = null, // optional //TODO: Is this required ?
        val invoiceNumber: String, //TODO : List of Invoices
        val supplier: AbstractParty,
        val funder: AbstractParty,
        val purchaseValue: Amount<Currency>,//Price
        val status: FundingResponseStatus
) : LinearState, QueryableState {


    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(FundingResponseSchemaV1)
    }

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is FundingResponseSchemaV1 -> FundingResponseSchemaV1Mapper().map(this)
            else -> throw IllegalArgumentException("Unrecognised schemas $schema")
        }    }

    override val participants: List<AbstractParty> get() = listOf(supplier, funder)

}