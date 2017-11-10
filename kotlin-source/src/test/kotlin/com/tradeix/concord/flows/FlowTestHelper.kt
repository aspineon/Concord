package com.tradeix.concord.flows

import com.tradeix.concord.flowmodels.TradeAssetAmendmentFlowModel
import com.tradeix.concord.flowmodels.TradeAssetIssuanceFlowModel
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork

object FlowTestHelper {
    fun issueTradeAsset(
            network: MockNetwork,
            initiator: StartedNode<MockNetwork.MockNode>,
            model: TradeAssetIssuanceFlowModel): SignedTransaction {

        val future = initiator
                .services
                .startFlow(TradeAssetIssuance.InitiatorFlow(model))
                .resultFuture

        network.runNetwork()

        return future.getOrThrow()
    }

    fun amendTradeAsset(
            network: MockNetwork,
            initiator: StartedNode<MockNetwork.MockNode>,
            model: TradeAssetAmendmentFlowModel): SignedTransaction {

        val future = initiator
                .services
                .startFlow(TradeAssetAmendment.InitiatorFlow(model))
                .resultFuture

        network.runNetwork()

        return future.getOrThrow()
    }
}