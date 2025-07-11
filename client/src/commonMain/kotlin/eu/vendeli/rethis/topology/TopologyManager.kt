package eu.vendeli.rethis.topology

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.configuration.RetryConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.EMPTY_BUFFER
import eu.vendeli.rethis.utils.withRetry
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.Buffer

internal interface TopologyManager {
    val retryCfg: RetryConfiguration

    suspend fun route(request: CommandRequest): ConnectionProvider
    suspend fun handleFailure(exception: Throwable)
    fun close()
}

internal suspend inline fun TopologyManager.handle(request: CommandRequest): Buffer = withRetry(retryCfg) {
    val currentCoCtx = currentCoroutineContext()
    val coLocalConn = currentCoCtx[CoLocalConn]
    val coPipeline = currentCoCtx[CoPipelineCtx]
    return runCatching {
        when {
            coPipeline != null -> {
                coPipeline.pipelined.add(request)
                EMPTY_BUFFER // todo handle in decoders
            }

            coLocalConn != null -> coLocalConn.connection.doRequest(request.buffer)

            else -> route(request).execute(request)
        }
    }.onFailure { handleFailure(it) }.getOrThrow()
}
