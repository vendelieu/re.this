package eu.vendeli.rethis

import eu.vendeli.rethis.annotations.ReThisDSL
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.core.ActiveSubscriptions
import eu.vendeli.rethis.core.ConnectionPool
import eu.vendeli.rethis.core.use
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.Const.DEFAULT_HOST
import eu.vendeli.rethis.utils.Const.DEFAULT_PORT
import eu.vendeli.rethis.utils.IO_OR_UNCONFINED
import eu.vendeli.rethis.utils.response.readListResponseTyped
import eu.vendeli.rethis.utils.response.readMapResponseTyped
import eu.vendeli.rethis.utils.response.readResponseWrapped
import eu.vendeli.rethis.utils.response.readSimpleResponseTyped
import io.ktor.util.logging.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

@ReThisDSL
class ReThis(
    internal val address: Address = Host(DEFAULT_HOST, DEFAULT_PORT),
    val protocol: RespVer = RespVer.V3,
    configurator: StandaloneConfiguration.() -> Unit = {},
) {

    internal val logger = KtorSimpleLogger("eu.vendeli.rethis.ReThis")
    internal val cfg: ReThisConfiguration = StandaloneConfiguration().apply(configurator)
    internal val rootJob = SupervisorJob()
    internal val coScope = CoroutineScope(
        rootJob + cfg.connectionConfiguration.dispatcher + CoroutineName("ReThis"),
    )
    internal val connectionPool = ConnectionPool(this)

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
    ): T? = performRequest(payload)
        ?.readSimpleResponseTyped(responseType, cfg.charset, jsonModule)

    @ReThisInternal
    suspend fun <T : Any> execute(
        payload: List<Argument>,
        responseType: TypeInfo,
        isCollectionResponse: Boolean = true,
        jsonModule: Json? = null,
    ): List<T>? = performRequest(payload)
        ?.readListResponseTyped(responseType, cfg.charset, jsonModule)

    @ReThisInternal
    suspend fun <K : Any, V : Any> execute(
        payload: List<Argument>,
        keyType: TypeInfo,
        valueType: TypeInfo,
        jsonModule: Json? = null,
    ): Map<K, V>? = performRequest(payload)
        ?.readMapResponseTyped(keyType, valueType, cfg.charset, jsonModule)

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
