package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.codecs.transaction.DiscardCommandCodec
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import eu.vendeli.rethis.codecs.transaction.MultiCommandCodec
import eu.vendeli.rethis.codecs.transaction.UnwatchCommandCodec
import eu.vendeli.rethis.codecs.transaction.WatchCommandCodec
import eu.vendeli.rethis.configuration.*
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.core.SubscriptionManager
import eu.vendeli.rethis.providers.DefaultConnectionProviderFactory
import eu.vendeli.rethis.providers.withConn
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.TransactionInvalidStateException
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.topology.CLUSTER_SLOT_COUNT
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
import io.ktor.utils.io.core.toByteArray
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
    internal val scope = CoroutineScope(rootJob + cfg.executionDispatcher + CoroutineName(CLIENT_NAME))

    val subscriptions = SubscriptionManager(this)
    val isActive get() = rootJob.isActive

    init {
        logger.info("Initialized ReThis with ${topology::class.simpleName}")
        logger.info("Using configuration: $cfg")
    }

    fun close() {
        logger.info("Shutting down")

        subscriptions.unsubscribeAll()
        topology.close()
        rootJob.cancel()
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
            scope
                .launch(currentCoroutineContext() + CoPipelineCtx(requests)) {
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

    /**
     * Runs [block] as a `MULTI` / `EXEC` transaction: commands sent inside are queued and answered with their
     * defaults, and the `EXEC` result is returned (`null` when the transaction was aborted by a `WATCH`).
     *
     * `WATCH` and `UNWATCH` are connection-scoped. Every plain client call borrows a pooled connection, so
     * `client.watch("k")` marks one connection while the next call, `unwatch` or this `transaction` may run on
     * another: the watched keys are then not observed by the `EXEC`, and the dirty flag can stay on the first
     * connection and leak to a later caller. Calling `watch` inside [block] is rejected by Redis, as it is sent
     * after `MULTI`. Optimistic locking (`WATCH`, read, `MULTI` / `EXEC`) therefore needs the keys watched on the
     * same connection the transaction runs on: run it inside [withConnection]. `watch` / `unwatch` on the pooled
     * client log a warning.
     */
    suspend fun transaction(block: suspend ReThis.() -> Unit): List<RType>? {
        val coLocalCon = currentCoroutineContext()[CoLocalConn]
        if (coLocalCon != null && coLocalCon.isTx) {
            logger.warn("Nested transaction detected")
            block()
            return emptyList()
        }

        val multiCommand = MultiCommandCodec.encode(cfg.charset)
        return withConn(topology.route(multiCommand), coLocalCon?.connection) { conn ->
            val tx = MultiCommandCodec.decode(
                conn.doRequest(multiCommand.data, commandTimeout = cfg.commandTimeout),
                cfg.charset,
            )
            if (!tx) throw TransactionInvalidStateException("Failed to start transaction")
            logger.debug { "Started transaction" }

            var e: Throwable? = null
            scope
                .launch(currentCoroutineContext() + CoLocalConn(conn, isTx = true)) {
                    runCatching { block() }.getOrElse { e = it }
                }.join()

            if (e != null) {
                conn.doRequest(DiscardCommandCodec.encode(cfg.charset).data, commandTimeout = cfg.commandTimeout)
                logger.error("Transaction canceled", e)
                throw TransactionInvalidStateException("Transaction canceled due to exception", e)
            }

            val exec = conn.doRequest(ExecCommandCodec.encode(cfg.charset).data, commandTimeout = cfg.commandTimeout)
            logger.debug { "Transaction completed" }

            ExecCommandCodec.decode(exec, cfg.charset)
        }
    }

    /**
     * Runs [block] on one pinned connection, so connection-scoped commands (`WATCH`, `UNWATCH`, `MULTI` / `EXEC`)
     * and the commands around them all reach the same server-side connection:
     *
     * ```
     * client.withConnection("balance") {
     *     watch("balance")
     *     val current = get("balance")
     *     transaction { set("balance", next(current)) } // null when "balance" changed after the watch
     * }
     * ```
     *
     * The connection is borrowed from the pool, `UNWATCH` is sent when [block] completes normally, and the
     * connection goes back to the pool; if [block] throws or is cancelled the connection is discarded, which also
     * drops any watch state it held. Pinned commands are not retried and cluster redirects are not followed.
     *
     * On a cluster the connection is chosen by [key]: the master owning the slot of that key, so pass one of the keys
     * that will be watched (all keys used in the block must hash to that slot, use a `{hash tag}`). Without [key] the
     * first known node is used, which is only right for single-node setups.
     *
     * A call nested in another [withConnection] or in a [transaction] reuses the already pinned connection and
     * ignores its own [key]; inside a pub/sub handler it reuses the subscribed connection. Commands issued
     * concurrently inside [block] (`async`, `launch`) interleave on that one socket, so keep the block sequential.
     */
    suspend fun <T> withConnection(key: String? = null, block: suspend ReThis.() -> T): T {
        if (currentCoroutineContext()[CoLocalConn] != null) return block()

        val routing = if (key != null) {
            WatchCommandCodec.encode(cfg.charset, key).let {
                if (cfg.withSlots) it.withSlot(CRC16.slot(key.toByteArray(cfg.charset)) % CLUSTER_SLOT_COUNT) else it
            }
        } else {
            UnwatchCommandCodec.encode(cfg.charset)
        }
        return withConn(topology.route(routing)) { conn ->
            val result = withContext(CoLocalConn(conn, isTx = false)) { block() }
            conn.doRequest(UnwatchCommandCodec.encode(cfg.charset).data, commandTimeout = cfg.commandTimeout)
            result
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

@Suppress("ktlint:standard:function-naming")
fun ReThis(
    host: String = DEFAULT_HOST,
    port: Int = DEFAULT_PORT,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
) = ReThis.standalone(Address(host, port), protocol, configurator)

/**
 * Creates and configures an instance of the `ReThis` client, which connects to a Redis server in standalone mode.
 *
 * @param address The Redis server URL in the format `redis://[[username]:password@]hostname[:port][/db]`
 *   (`rediss://` enables TLS with the default `TLSConfig` unless the configurator calls `tls {}`; IPv6 hosts in brackets,
 *   `%xx` escapes in username and password are decoded).
 *   A malformed or unsupported URL throws [IllegalArgumentException]; the message never
 *   contains the URL because it can carry credentials.
 * @param protocol The RESP (Redis Serialization Protocol) version to use for communication.
 *                 Defaults to `RespVer.V2`.
 * @param configurator A lambda to configure the `StandaloneConfiguration`.
 *                     Defaults to an empty configuration.
 * @return A new instance of `ReThis` configured for standalone mode.
 * @throws IllegalArgumentException if [address] is not a valid `redis://` or `rediss://` URL.
 */
@Suppress("ktlint:standard:function-naming")
fun ReThis(
    address: String,
    protocol: RespVer = RespVer.V2,
    configurator: StandaloneConfiguration.() -> Unit = {},
): ReThis {
    val addr = UrlAddress(address)
    val cfg = StandaloneConfiguration(protocol)
    cfg.db = addr.db
    addr.authConfiguration()?.let { cfg.auth = it }

    cfg.configurator()
    cfg.applyUrlTls(addr.useTls)

    return ReThis(cfg) { StandaloneTopologyManager(addr.address, this) }
}
