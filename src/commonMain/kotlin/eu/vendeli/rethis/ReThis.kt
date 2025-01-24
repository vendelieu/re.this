package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.*
import eu.vendeli.rethis.utils.Const.DEFAULT_HOST
import eu.vendeli.rethis.utils.Const.DEFAULT_PORT
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

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
    internal val rethisCoScope = CoroutineScope(
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

    fun disconnect() = connectionPool.disconnect()
    fun reconnect() {
        if (connectionPool.isEmpty) connectionPool.prepare()
    }

    fun shutdown() = runBlocking {
        disconnect()
        rootJob.cancelAndJoin()
    }

    suspend fun pipeline(block: suspend ReThis.() -> Unit): List<RType> {
        val pipelineCtx = takeFromCoCtx(CoPipelineCtx)
        var ctxConn: Connection? = null
        if (pipelineCtx == null) {
            val requests = mutableListOf<Any?>()
            logger.info("Pipeline started")
            try {
                rethisCoScope
                    .launch(currentCoroutineContext() + CoPipelineCtx(requests)) {
                        block()
                        ctxConn = currentCoroutineContext()[CoLocalConn]?.connection
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
            return if (connection != null) runBlocking {
                connection.sendRequest(pipelinedPayload)
                requests.map { connection.parseResponse() }
            }.map {
                it.readResponseWrapped(cfg.charset)
            } else connectionPool
                .use { connection ->
                    runBlocking {
                        connection.sendRequest(pipelinedPayload)
                        requests.map { connection.parseResponse() }
                    }
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
                .sendRequest(listOf("MULTI".toArg()))
                .parseResponse()
            if (!multiRequest.readResponseWrapped(cfg.charset).isOk())
                throw ReThisException("Failed to start transaction")

            var e: Throwable? = null
            rethisCoScope
                .launch(currentCoroutineContext() + CoLocalConn(conn)) {
                    runCatching { block() }.getOrElse { e = it }
                }.join()
            e?.also {
                val discardRequest = conn
                    .sendRequest(listOf("DISCARD".toArg()))
                    .parseResponse()
                if (!discardRequest.readResponseWrapped(cfg.charset).isOk())
                    throw ReThisException("Failed to cancel transaction")
                logger.error("Transaction canceled", it)
                throw it
            }

            logger.debug("Transaction completed")
            conn
                .sendRequest(listOf("EXEC".toArg()))
                .parseResponse()
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
        rawResponse: Boolean = false,
    ): RType = rethisCoScope
        .async(Dispatchers.IO) {
            handleRequest(payload)
        }.await()
        ?.readResponseWrapped(cfg.charset, rawResponse) ?: RType.Null

    @JvmName("executeSimple")
    internal suspend inline fun <reified T : Any> execute(
        payload: List<Argument>,
    ): T? = rethisCoScope
        .async(currentCoroutineContext() + Dispatchers.IO) {
            handleRequest(payload)
        }.await()
        ?.readSimpleResponseTyped(T::class, cfg.charset)

    @JvmName("executeList")
    internal suspend inline fun <reified T : Any> execute(
        payload: List<Argument>,
        isCollectionResponse: Boolean = false,
    ): List<T>? = rethisCoScope
        .async(currentCoroutineContext() + Dispatchers.IO) {
            handleRequest(payload)
        }.await()
        ?.readListResponseTyped(T::class, cfg.charset)

    @JvmName("executeMap")
    internal suspend inline fun <reified K : Any, reified V : Any> execute(
        payload: List<Argument>,
    ): Map<K, V?>? = rethisCoScope
        .async(currentCoroutineContext() + Dispatchers.IO) {
            handleRequest(payload)
        }.await()
        ?.readMapResponseTyped(K::class, V::class, cfg.charset)

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

            coLocalConn != null -> {
                runBlocking {
                    coLocalConn.connection
                        .sendRequest(payload)
                        .parseResponse()
                }
            }

            else -> connectionPool.use { connection ->
                runBlocking {
                    connection.sendRequest(payload).parseResponse()
                }
            }
        }
    }

    private suspend fun Connection.sendRequest(payload: List<Argument>): Connection = apply {
        logger.trace { "Sending request with such payload $payload" }
        output.writeBuffer(bufferValues(payload, cfg.charset))
        output.flush()
    }
}
