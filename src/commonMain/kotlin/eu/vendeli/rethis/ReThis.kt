package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.core.*
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.*
import eu.vendeli.rethis.utils.Const.DEFAULT_HOST
import eu.vendeli.rethis.utils.Const.DEFAULT_PORT
import eu.vendeli.rethis.utils.response.readListResponseTyped
import eu.vendeli.rethis.utils.response.readMapResponseTyped
import eu.vendeli.rethis.utils.response.readResponseWrapped
import eu.vendeli.rethis.utils.response.readSimpleResponseTyped
import io.ktor.util.logging.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import kotlinx.serialization.json.Json

@ReThisDSL
class ReThis(
    internal val address: Address = Host(DEFAULT_HOST, DEFAULT_PORT),
    val protocol: RespVer = RespVer.V3,
    configurator: ClientConfiguration.() -> Unit = {},
) {
    constructor(
        host: String = DEFAULT_HOST,
        port: Int = DEFAULT_PORT,
        protocol: RespVer = RespVer.V3,
        configurator: ClientConfiguration.() -> Unit = {},
    ) : this(Host(host, port), protocol, configurator)

    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ReThis")
    internal val cfg: ClientConfiguration = ClientConfiguration().apply(configurator)
    internal val rootJob = SupervisorJob()
    internal val coScope = CoroutineScope(
        rootJob + cfg.connectionConfiguration.dispatcher + CoroutineName("ReThis"),
    )
    internal val connectionPool = ConnectionPool(this)

    init {
        if (address is Url) {
            cfg.db = address.db
            if (address.credentials.isNotEmpty()) {
                cfg.auth = AuthConfiguration(
                    password = address.credentials.last(),
                    username = address.credentials.takeIf { it.size > 1 }?.first(),
                )
            }
        }

        buildString {
            append("Created ReThis client.\n")
            append("Address: $address\n")
            append("DB: ${cfg.db ?: 0}\n")
            append("Auth: ${cfg.auth != null}\n")
            append("TLS: ${cfg.tlsConfig != null}\n")
            append("Pool size: ${cfg.connectionConfiguration.poolSize}\n")
            append("Protocol: ${protocol}\n")
        }.let { logger.info(it) }
    }

    val subscriptions = ActiveSubscriptions()
    val isDisconnected: Boolean get() = connectionPool.isEmpty

    suspend fun disconnect() = connectionPool.disconnect()
    fun reconnect() {
        if (connectionPool.isEmpty) connectionPool.prepare()
    }

    suspend fun shutdown() {
        disconnect()
        rootJob.cancelAndJoin()
    }

    suspend fun pipeline(block: suspend ReThis.() -> Unit): List<RType> {
        val pipelineCtx = takeFromCoCtx(CoPipelineCtx)
        var ctxConn: RConnection? = null
        if (pipelineCtx == null) {
            val requests = mutableListOf<List<Argument>>()
            logger.info("Pipeline started")
            try {
                coScope
                    .launch(currentCoroutineContext() + CoPipelineCtx(requests)) {
                        block()
                        ctxConn = takeFromCoCtx(CoLocalConn)?.connection
                    }.join()
            } catch (e: Throwable) {
                logger.error("Pipeline removed")
                requests.clear()
                throw e
            }
            val pipelinedPayload = Buffer().apply {
                requests.forEach { writeRedisValue(it, cfg.charset) }
            }
            logger.debug("Executing pipelined request\nRequest payload: $requests")

            val connection = ctxConn
            return if (connection != null) coScope
                .async {
                    connection.sendRequest(pipelinedPayload).readBatchResponse(requests.size)
                }.await()
                .map {
                    it.readResponseWrapped(cfg.charset)
                } else connectionPool
                .use { connection ->
                    coScope
                        .async {
                            connection
                                .sendRequest(pipelinedPayload)
                                .readBatchResponse(requests.size)
                        }.await()
                }.map {
                    it.readResponseWrapped(cfg.charset)
                }.also {
                    requests.clear()
                    logger.info("Pipeline finished")
                    logger.trace { "Such responses returned $it" }
                }
        } else {
            logger.warn("Nested pipeline detected")
            block()
            return emptyList()
        }
    }

    suspend fun transaction(block: suspend ReThis.() -> Unit): List<RType> {
        val coLocalCon = takeFromCoCtx(CoLocalConn)
        if (coLocalCon != null) {
            logger.warn("Nested transaction detected")
            block()
            return emptyList()
        }

        return connectionPool.use { conn ->
            logger.debug("Started transaction")
            val multiRequest = conn
                .exchangeData(listOf("MULTI".toArgument()), cfg.charset)

            if (!multiRequest.readResponseWrapped(cfg.charset).isOk())
                throw InvalidStateException("Failed to start transaction")

            var e: Throwable? = null
            coScope
                .launch(currentCoroutineContext() + CoLocalConn(conn)) {
                    runCatching { block() }.getOrElse { e = it }
                }.join()
            e?.also {
                val discardRequest = conn
                    .exchangeData(listOf("DISCARD".toArgument()), cfg.charset)

                if (!discardRequest.readResponseWrapped(cfg.charset).isOk())
                    throw InvalidStateException("Failed to cancel transaction")
                logger.error("Transaction canceled", it)
                throw it
            }

            logger.debug("Transaction completed")
            coScope
                .async {
                    conn
                        .exchangeData(listOf("EXEC".toArgument()), cfg.charset)
                }.await()
                .readResponseWrapped(cfg.charset)
                .unwrapList<RType>()
                .also {
                    logger.debug("Response payload: $it")
                }
        }
    }

    @ReThisInternal
    suspend fun execute(
        payload: List<Argument>,
        rawMarker: Unit? = null,
    ): RType = performRequest(payload)
        ?.readResponseWrapped(charset = cfg.charset, rawOnly = rawMarker != null) ?: RType.Null

    @ReThisInternal
    suspend fun <T : Any> execute(
        payload: List<Argument>,
        responseType: TypeInfo,
        jsonModule: Json? = null,
    ) = performRequest(payload)
        ?.readSimpleResponseTyped<T>(responseType, cfg.charset, jsonModule)

    @ReThisInternal
    suspend fun <T : Any> execute(
        payload: List<Argument>,
        responseType: TypeInfo,
        isCollectionResponse: Boolean = true,
        jsonModule: Json? = null,
    ) = performRequest(payload)
        ?.readListResponseTyped<T>(responseType, cfg.charset, jsonModule)

    @ReThisInternal
    suspend fun <K : Any, V : Any> execute(
        payload: List<Argument>,
        keyType: TypeInfo,
        valueType: TypeInfo,
        jsonModule: Json? = null,
    ) = performRequest(payload)
        ?.readMapResponseTyped<K, V>(keyType, valueType, cfg.charset, jsonModule)

    private suspend inline fun performRequest(
        payload: List<Argument>,
    ): ArrayDeque<ResponseToken>? = coScope
        .async(currentCoroutineContext() + Dispatchers.IO_OR_UNCONFINED) {
            logger.trace { "Performing request with payload $payload" }
            handleRequest(payload)
        }.await()

    private suspend fun handleRequest(
        payload: List<Argument>,
    ): ArrayDeque<ResponseToken>? {
        val currentCoCtx = currentCoroutineContext()
        val coLocalConn = currentCoCtx[CoLocalConn]
        val coPipeline = currentCoCtx[CoPipelineCtx]
        return when {
            coPipeline != null -> {
                coPipeline.pipelinedRequests.add(payload)
                null
            }

            coLocalConn != null ->
                coLocalConn.connection
                    .exchangeData(payload, cfg.charset)

            else -> connectionPool.use { connection ->
                coScope.async { connection.exchangeData(payload, cfg.charset) }.await()
            }
        }
    }
}
