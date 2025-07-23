package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.InvalidStateException
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.codecs.transaction.DiscardCommandCodec
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import eu.vendeli.rethis.codecs.transaction.MultiCommandCodec
import eu.vendeli.rethis.configuration.*
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.core.SubscriptionManager
import eu.vendeli.rethis.providers.DefaultConnectionProviderFactory
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.topology.ClusterTopologyManager
import eu.vendeli.rethis.topology.SentinelTopologyManager
import eu.vendeli.rethis.topology.StandaloneTopologyManager
import eu.vendeli.rethis.topology.TopologyManager
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.UrlAddress
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.CLIENT_NAME
import eu.vendeli.rethis.utils.DEFAULT_HOST
import eu.vendeli.rethis.utils.DEFAULT_PORT
import eu.vendeli.rethis.utils.handlePipelinedRequests
import io.ktor.util.logging.*
import kotlinx.coroutines.*

@ReThisDSL
class ReThis internal constructor(
    internal val cfg: ReThisConfiguration,
    topologyBlock: ReThis.() -> TopologyManager,
) {
    internal val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ReThis")
    internal val rootJob = SupervisorJob()
    internal val connectionFactory = ConnectionFactory(cfg, rootJob)
    internal val connectionProviderFactory = DefaultConnectionProviderFactory(this)
    internal val topology = topologyBlock()
    internal val scope = CoroutineScope(rootJob + cfg.dispatcher + CoroutineName(CLIENT_NAME))

    val subscriptions = SubscriptionManager()
    val isActive get() = rootJob.isActive

    fun close() {
        logger.info("Shutting down")

        subscriptions.unsubscribeAll()
        topology.close()
        scope.cancel()
    }

    suspend fun pipeline(block: suspend ReThis.() -> Unit): List<RType> {
        val pipelineCtx = currentCoroutineContext()[CoPipelineCtx]
        var ctxConn: RConnection? = null
        if (pipelineCtx != null) {
            logger.warn("Nested pipeline detected")
            block()
            return emptyList()
        }
        val requests = mutableListOf<CommandRequest>()
        logger.info("Pipeline started")
        try {
            scope.launch(currentCoroutineContext() + CoPipelineCtx(requests)) {
                block()
                ctxConn = currentCoroutineContext()[CoLocalConn]?.connection
            }.join()
        } catch (e: Throwable) {
            logger.error("Pipeline removed")
            requests.clear()
            throw e
        }
        val pipelinedPayload = handlePipelinedRequests(requests, ctxConn)
        logger.debug { "Executing pipelined request\nRequest payload: $requests" }
        logger.trace { "Pipelined response: $pipelinedPayload" }
        requests.clear()

        return pipelinedPayload
    }

    suspend fun transaction(block: suspend ReThis.() -> Unit): List<RType>? {
        val coLocalCon = currentCoroutineContext()[CoLocalConn]
        if (coLocalCon != null) {
            logger.warn("Nested transaction detected")
            block()
            return emptyList()
        }

        val multiCommand = MultiCommandCodec.encode(cfg.charset)
        return topology.route(multiCommand).withConnection { conn ->
            val tx = MultiCommandCodec.decode(conn.doRequest(multiCommand.buffer), cfg.charset)
            if (!tx) throw InvalidStateException("Failed to start transaction")
            logger.debug { "Started transaction" }

            var e: Throwable? = null
            try {
                scope.launch(currentCoroutineContext() + CoLocalConn(conn)) {
                    runCatching { block() }.getOrElse { e = it }
                }.join()

                val exec = conn.doRequest(ExecCommandCodec.encode(cfg.charset).buffer)
                logger.debug { "Transaction completed" }

                ExecCommandCodec.decode(exec, cfg.charset)
            } catch (ex: Throwable) {
                throw ReThisException("Caught exception in transaction", ex).also {
                    if (e != null) it.addSuppressed(e)
                }
            } finally {
                if (e != null) {
                    conn.doRequest(DiscardCommandCodec.encode(cfg.charset).buffer)
                    logger.error("Transaction canceled", e)
                    throw e
                }
            }
        }
    }

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
