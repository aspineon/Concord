package com.tradeix.concord.messages

import net.corda.core.contracts.UniqueIdentifier
import java.math.BigDecimal

data class TradeAssetAmendmentRequestMessage(
        override val correlationId: String?,
        override val tryCount: Int,
        val externalId: String?,
        val value: BigDecimal?,
        val currency: String?
) : Message() {
    val linearId: UniqueIdentifier get() = UniqueIdentifier(externalId!!)
}