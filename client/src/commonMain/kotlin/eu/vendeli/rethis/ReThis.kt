package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.configuration.*
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.core.SubscriptionManager
import eu.vendeli.rethis.providers.DefaultConnectionProviderFactory
import eu.vendeli.rethis.topology.ClusterTopologyManager
import eu.vendeli.rethis.topology.SentinelTopologyManager
import eu.vendeli.rethis.topology.StandaloneTopologyManager
import eu.vendeli.rethis.topology.TopologyManager
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.UrlAddress
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.DEFAULT_HOST
import eu.vendeli.rethis.utils.DEFAULT_PORT
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@ReThisDSL
class ReThis internal constructor(
    internal val cfg: ReThisConfiguration,
    topologyBlock: ReThis.() -> TopologyManager,
) {
    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ReThis")
    internal val rootJob = SupervisorJob()
    internal val connectionFactory = ConnectionFactory(cfg, rootJob)
    internal val topology = topologyBlock()
    internal val connectionProviderFactory = DefaultConnectionProviderFactory(this)
    internal val scope = CoroutineScope(rootJob + cfg.dispatcher + CoroutineName(CLIENT_NAME))

    val subscriptions = SubscriptionManager()

    fun shutdown() {
        logger.info("Shutting down")

        subscriptions.unsubscribeAll()
        topology.close()
        scope.cancel()
    }

    // pipeline
    // transaction

    companion object {
        fun standalone(
            address: Address,
            protocol: RespVer = RespVer.V2,
            configurator: StandaloneConfiguration.() -> Unit = {},
        ): ReThis {
            val cfg = StandaloneConfiguration(protocol)
            cfg.configurator()

            return ReThis(cfg) { StandaloneTopologyManager(address, this) }
        }

        fun cluster(
            initialNodes: List<Address>,
            protocol: RespVer = RespVer.V2,
            configurator: ClusterConfiguration.() -> Unit = {},
        ): ReThis {
            val cfg = ClusterConfiguration(protocol)
            cfg.configurator()

            return ReThis(cfg) { ClusterTopologyManager(initialNodes, this, cfg) }
        }

        fun sentinel(
            masterName: String,
            sentinelNodes: List<Address>,
            protocol: RespVer = RespVer.V2,
            configurator: SentinelConfiguration.() -> Unit = {},
        ): ReThis {
            val cfg = SentinelConfiguration(protocol)
            cfg.configurator()

            return ReThis(cfg) { SentinelTopologyManager(masterName, sentinelNodes, this, cfg) }
        }
    }
}

fun ReThis(
    host: String = DEFAULT_HOST,
    port: Int = DEFAULT_PORT,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
) = ReThis.standalone(Address(host, port), protocol, configurator)

fun ReThis(
    address: String,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
): ReThis {
    val addr = UrlAddress(address)
    val cfg = StandaloneConfiguration(protocol)
    cfg.db = addr.db
    addr.credentials.takeIf { it.isNotEmpty() }?.also { credentials ->
        cfg.auth = AuthConfiguration(
            credentials.first().toCharArray(), credentials.takeIf { it.size > 1 }?.last(),
        )
    }

    cfg.configurator()

    return ReThis(cfg) { StandaloneTopologyManager(addr.address, this) }
}
