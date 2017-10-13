package com.tradeix.concord.plugins

import com.tradeix.concord.apis.NodeInfoApi
import com.tradeix.concord.apis.TradeAssetApi
import net.corda.core.messaging.CordaRPCOps
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class ConcordPlugin : WebServerPluginRegistry {
    /**
     * A list of classes that expose web APIs.
     */
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(
            Function(::NodeInfoApi),
            Function(::TradeAssetApi))

    /**
     * A list of directories in the resources directory that will be served by Jetty under /web.
     */
    override val staticServeDirs: Map<String, String> = mapOf(
            // This will serve the exampleWeb directory in resources to /web/example
            "tix" to javaClass.classLoader.getResource("cordax/dist").toExternalForm()
    )
}