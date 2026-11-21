package eu.vendeli.rethis.topology

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.decoders.general.BulkErrorDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.RespUnexpectedEOF
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.types.interfaces.CommandOutcome
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.utils.isRetryableFor
import eu.vendeli.rethis.utils.isTransientFailure
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
internal suspend fun TopologyManager.handle(request: CommandRequest): Buffer {
    // Decide once whether this request is wire-bound; pipeline-accumulated and tx-QUEUED
    // requests don't reach the wire and shouldn't open an observation.
    val initialCtx = currentCoroutineContext()
    val tracked = initialCtx[CoPipelineCtx] == null && initialCtx[CoLocalConn]?.isTx != true
    if (initialCtx[CoPipelineCtx] == null && initialCtx[CoLocalConn] == null && request.isConnectionStateful()) {
        warnOfPooledConnectionState(cfg, request.command)
    }
    val obs = if (tracked) cfg.metricsRecorder?.commandStarted(request.command) else null
    var attempts = 0
    try {
        // A pinned connection that failed is dead (or its MULTI/WATCH state is gone): retrying on it cannot succeed.
        val pinned = initialCtx[CoLocalConn] != null
        val result = withRetry(cfg, { !pinned && it.isRetryableFor(request.operation) }) { attempt ->
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
                            .doRequest(request.data, commandTimeout = cfg.commandTimeoutFor(request))
                            .also {
                                // peek the type byte without consuming: reads move the buffer head,
                                // and writeByte would append the byte to the tail, corrupting the frame
                                if (!it.exhausted()) when (val code = RespCode.fromCode(it.peek().readByte())) {
                                    RespCode.SIMPLE_ERROR -> {
                                        SimpleErrorDecoder.decode(it.apply { skip(1) }, cfg.charset, code)
                                    }

                                    RespCode.BULK_ERROR -> {
                                        BulkErrorDecoder.decode(it.apply { skip(1) }, cfg.charset, code)
                                    }

                                    else -> {}
                                }
                            }.takeIf { !coLocalConn.isTx } ?: run {
                            warnOfSubstitution(cfg)
                            // return empty buffer if transaction (to not break response contract since transaction return QUEUED)
                            EMPTY_BUFFER
                        }
                    }

                    else -> {
                        route(request).execute(request).also { it.throwIfRetryableError() }
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

/**
 * Raises a cluster redirect (MOVED, ASK, TRYAGAIN) or cluster-down error reply here, inside the retried call, so that
 * [TopologyManager.handleFailure] can act on it; the codec would only raise it after the retry scope has been left.
 * Any other reply, including other error replies, is left to the codec. The buffer is not consumed.
 */
internal fun Buffer.throwIfRetryableError() {
    if (exhausted()) return
    val code = RespCode.fromCode(peek().readByte())
    if (code != RespCode.SIMPLE_ERROR && code != RespCode.BULK_ERROR) return
    copy()
        .apply { skip(1) }
        .tryInferCause(code)
        ?.takeIf { it.isTransientFailure() }
        ?.let { throw it }
}

@OptIn(ExperimentalReThisMetricsApi::class)
@PublishedApi
internal fun classifyCommandOutcome(e: Throwable): CommandOutcome = when {
    e is CommandTimeoutException -> CommandOutcome.ERROR_TIMEOUT
    e is IOException -> CommandOutcome.ERROR_IO
    e is RespUnexpectedEOF -> CommandOutcome.ERROR_IO
    e is ReThisException -> CommandOutcome.ERROR_REDIS
    else -> CommandOutcome.ERROR_OTHER
}

private val CONNECTION_STATEFUL_COMMANDS = setOf("WATCH", "UNWATCH", "MULTI", "EXEC", "DISCARD")

/** True for commands whose effect lives on the server-side connection, so a pooled connection cannot keep it. */
internal fun CommandRequest.isConnectionStateful(): Boolean = command in CONNECTION_STATEFUL_COMMANDS

private fun warnOfPooledConnectionState(cfg: ReThisConfiguration, command: String) {
    cfg.loggerFactory
        .get("eu.vendeli.rethis.topology.TopologyManager")
        .warn(
            "$command is connection-scoped but was sent through the connection pool: the state it sets stays " +
                "on whichever pooled connection handled it and is not seen by the next command or transaction. " +
                "Run the sequence inside ReThis.withConnection to keep it on one connection.",
        )
}

private inline fun warnOfSubstitution(cfg: ReThisConfiguration) {
    cfg.loggerFactory
        .get("eu.vendeli.rethis.topology.TopologyManager")
        .debug {
            "Response substituted to EMPTY_BUFFER " +
                "and will be handled as default response since it been executed in special construction"
        }
}
