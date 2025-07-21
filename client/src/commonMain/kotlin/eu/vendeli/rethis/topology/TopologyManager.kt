package eu.vendeli.rethis.topology

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.configuration.RetryConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.withRetry
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.Buffer

internal interface TopologyManager {
    val cfg: ReThisConfiguration

    suspend fun route(request: CommandRequest): ConnectionProvider
    suspend fun handleFailure(request: CommandRequest, exception: Throwable): Buffer = throw exception
    fun close()
}

internal suspend inline fun TopologyManager.handle(request: CommandRequest): Buffer = withRetry(cfg) {
    val currentCoCtx = currentCoroutineContext()
    val coLocalConn = currentCoCtx[CoLocalConn]
    val coPipeline = currentCoCtx[CoPipelineCtx]
    return runCatching {
        when {
            coPipeline != null -> {
                coPipeline.pipelined.add(request)
                EMPTY_BUFFER
            }

            coLocalConn != null -> coLocalConn.connection.doRequest(request.buffer)

            else -> route(request).execute(request)
        }
    }.getOrElse { handleFailure(request, it) }
}
