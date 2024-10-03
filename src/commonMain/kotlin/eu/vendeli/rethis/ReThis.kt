package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.Const.DEFAULT_HOST
import eu.vendeli.rethis.utils.Const.DEFAULT_PORT
import eu.vendeli.rethis.utils.bufferValues
import eu.vendeli.rethis.utils.coLaunch
import eu.vendeli.rethis.utils.readRedisMessage
import eu.vendeli.rethis.utils.writeRedisValue
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.Buffer

class ReThis(
    address: SocketAddress = InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT),
    val protocol: RespVer = RespVer.V3,
    configurator: ClientConfiguration.() -> Unit = {},
) {
    constructor(
        host: String,
        port: Int,
        protocol: RespVer = RespVer.V3,
        configurator: ClientConfiguration.() -> Unit = {},
    ) : this(InetSocketAddress(host, port), protocol, configurator)

    val logger = KtorSimpleLogger("eu.vendeli.rethis.ReThis")
    internal val cfg: ClientConfiguration = ClientConfiguration().apply(configurator)
    internal val rootJob = SupervisorJob()

    internal val subscriptionHandlers = mutableMapOf<String, Job>()
    internal val connectionPool by lazy { ConnectionPool(this, address).also { it.prepare() } }

    init {
        logger.info("Created client (RESP $protocol)")
    }

    val subscriptions: Map<String, Job> get() = subscriptionHandlers
    val isDisconnected: Boolean get() = connectionPool.isEmpty

    fun disconnect() = connectionPool.disconnect()
    fun reconnect() = connectionPool.prepare()

    @ReThisInternal
    suspend fun execute(payload: Any?, forceBulk: Boolean = true, rawResponse: Boolean = false): RType {
        val currentCoCtx = currentCoroutineContext()
        val coLocalConn = currentCoCtx[CoLocalConn]
        val coPipeline = currentCoCtx[CoPipelineCtx]
        return when {
            coPipeline != null -> {
                coPipeline.pipelinedRequests.add(payload)
                RType.Null
            }

            coLocalConn != null -> {
                coLocalConn.connection.exec(payload, forceBulk, rawResponse)
            }

            else -> connectionPool.use { connection ->
                connection.exec(payload, forceBulk, rawResponse)
            }
        }
    }

    suspend fun pipeline(block: suspend ReThis.() -> Unit): List<RType> {
        val responses = mutableListOf<RType>()
        val isCoroutinePipelined = currentCoroutineContext()[CoPipelineCtx] != null
        var ctxConn: Connection? = null
        if (!isCoroutinePipelined) {
            val requests = mutableListOf<Any?>()
            logger.info("Pipeline started")
            try {
                coLaunch(currentCoroutineContext() + CoPipelineCtx(requests)) {
                    block()
                    coroutineContext[CoLocalConn]?.also { ctxConn = it.connection }
                }.join()
            } catch (e: Throwable) {
                logger.error("Pipeline removed")
                requests.clear()
                throw e
            }
            val pipelinedPayload = Buffer().apply {
                requests.forEach { writeRedisValue(it) }
            }
            logger.debug("Executing pipelined request")
            if (ctxConn != null) {
                ctxConn!!.output.writeBuffer(pipelinedPayload)
                ctxConn!!.output.flush()
                requests.forEach { _ -> responses.add(ctxConn!!.input.readRedisMessage()) }
            } else {
                connectionPool.use { connection ->
                    connection.output.writeBuffer(pipelinedPayload)
                    connection.output.flush()
                    requests.forEach { _ -> responses.add(connection.input.readRedisMessage()) }
                }
            }
            requests.clear()
        }
        logger.info("Pipeline finished")
        return responses
    }

    suspend fun transaction(block: suspend ReThis.() -> Unit): List<RType> = connectionPool.use { conn ->
        logger.debug("Started transaction")
        conn.output.writeBuffer(bufferValues(listOf("MULTI")))
        conn.output.flush()
        require(conn.input.readRedisMessage().value == "OK")

        var e: Throwable? = null
        coLaunch(currentCoroutineContext() + CoLocalConn(conn)) {
            runCatching { block() }.getOrElse { e = it }
        }.join()
        e?.also {
            conn.output.writeBuffer(bufferValues(listOf("DISCARD")))
            conn.output.flush()
            require(conn.input.readRedisMessage().value == "OK")
            logger.error("Transaction canceled", it)
            return@use emptyList()
        }

        logger.debug("Transaction completed")
        conn.output.writeBuffer(bufferValues(listOf("EXEC")))
        conn.output.flush()
        conn.input.readRedisMessage().unwrapList()
    }

    private suspend inline fun Connection.exec(
        payload: Any?,
        forceBulk: Boolean,
        raw: Boolean = false,
    ): RType {
        logger.trace("Executing request with such payload $payload [forceBulk: $forceBulk]")
        output.writeBuffer(bufferValues(payload, forceBulk))
        output.flush()
        return input.readRedisMessage(raw)
    }
}