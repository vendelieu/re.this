package eu.vendeli.rethis.topology

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.decoders.general.BulkErrorDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.types.interfaces.CommandOutcome
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.utils.withRetry
import io.ktor.util.logging.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.io.Buffer
import kotlinx.io.IOException

internal interface TopologyManager {
    val cfg: ReThisConfiguration

    suspend fun route(request: CommandRequest): ConnectionProvider
    suspend fun handleFailure(request: CommandRequest, exception: Throwable): Buffer = throw exception
    fun close()
}

@OptIn(ExperimentalReThisMetricsApi::class)
internal suspend inline fun TopologyManager.handle(request: CommandRequest): Buffer {
    // Decide once whether this request is wire-bound; pipeline-accumulated and tx-QUEUED
    // requests don't reach the wire and shouldn't open an observation.
    val initialCtx = currentCoroutineContext()
    val tracked = initialCtx[CoPipelineCtx] == null && initialCtx[CoLocalConn]?.isTx != true
    val obs = if (tracked) cfg.metricsRecorder?.commandStarted(request.command) else null
    var attempts = 0
    try {
        val result = withRetry(cfg) { attempt ->
            attempts = attempt + 1
            val currentCoCtx = currentCoroutineContext()
            val coLocalConn = currentCoCtx[CoLocalConn]
            val coPipeline = currentCoCtx[CoPipelineCtx]
            runCatching {
                when {
                    coPipeline != null -> {
                        coPipeline.pipelined.add(request)
                        warnOfSubstitution(cfg)
                        EMPTY_BUFFER
                    }

                    coLocalConn != null -> {
                        coLocalConn.connection
                            .doRequest(request.data)
                            .also {
                                val peekedByte = it.readByte()
                                when (val code = RespCode.fromCode(peekedByte)) {
                                    RespCode.SIMPLE_ERROR -> SimpleErrorDecoder.decode(it, cfg.charset, code)
                                    RespCode.BULK_ERROR -> BulkErrorDecoder.decode(it, cfg.charset, code)
                                    else -> it.writeByte(peekedByte)
                                }
                            }.takeIf { !coLocalConn.isTx } ?: run {
                            warnOfSubstitution(cfg)
                            // return empty buffer if transaction (to not break response contract since transaction return QUEUED)
                            EMPTY_BUFFER
                        }
                    }

                    else -> {
                        route(request).execute(request)
                    }
                }
            }.getOrElse { handleFailure(request, it) }
        }
        obs?.completed(attempts, CommandOutcome.OK)
        return result
    } catch (e: Throwable) {
        obs?.completed(attempts, classifyCommandOutcome(e))
        throw e
    }
}

@OptIn(ExperimentalReThisMetricsApi::class)
@PublishedApi
internal fun classifyCommandOutcome(e: Throwable): CommandOutcome = when {
    e is CommandTimeoutException -> CommandOutcome.ERROR_TIMEOUT
    e is IOException -> CommandOutcome.ERROR_IO
    e::class == ReThisException::class -> CommandOutcome.ERROR_REDIS
    else -> CommandOutcome.ERROR_OTHER
}

private inline fun warnOfSubstitution(cfg: ReThisConfiguration) {
    cfg.loggerFactory
        .get("eu.vendeli.rethis.topology.TopologyManager")
        .debug {
            "Response substituted to EMPTY_BUFFER " +
                "and will be handled as default response since it been executed in special construction"
        }
}
