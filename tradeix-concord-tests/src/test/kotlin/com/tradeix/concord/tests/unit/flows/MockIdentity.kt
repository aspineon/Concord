package com.tradeix.concord.tests.unit.flows

import com.tradeix.concord.shared.mockdata.ParticipantType
import net.corda.testing.internal.chooseIdentity
import net.corda.testing.node.StartedMockNode

data class MockIdentity(val node: StartedMockNode, val type: ParticipantType) {

    val party get() = node.info.chooseIdentity()
    val name get() = party.name
    val publicKey get() = party.owningKey

    override fun toString() = party.name.toString()
}