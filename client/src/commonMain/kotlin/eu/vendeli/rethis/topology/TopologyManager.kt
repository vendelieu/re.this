package eu.vendeli.rethis.topology

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.decoders.general.BulkErrorDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.coroutine.CoPipelineCtx
import eu.vendeli.rethis.utils.withRetry
import io.ktor.util.logging.*
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
                warnOfSubstitution(cfg)
                EMPTY_BUFFER
            }

            coLocalConn != null -> {
                coLocalConn.connection
                    .doRequest(request.buffer)
                    .also {
                        val peekedByte = it.readByte()
                        when (val code = RespCode.fromCode(peekedByte)) {
                            RespCode.SIMPLE_ERROR -> SimpleErrorDecoder.decode(it, cfg.charset, code)
                            RespCode.BULK_ERROR -> BulkErrorDecoder.decode(it, cfg.charset, code)
                            else -> it.writeByte(peekedByte)
                        }
                    }.takeIf { !coLocalConn.isTx } ?: run {
                    warnOfSubstitution(cfg)
                    EMPTY_BUFFER
                }
            }

            // return empty buffer if transaction (to not break response contract since transaction return QUEUED)

            else -> {
                route(request).execute(request)
            }
        }
    }.getOrElse { handleFailure(request, it) }
}

private inline fun warnOfSubstitution(cfg: ReThisConfiguration) {
    cfg.loggerFactory
        .get("eu.vendeli.rethis.topology.TopologyManager")
        .debug {
            "Response substituted to EMPTY_BUFFER " +
                "and will be handled as default response since it been executed in special construction"
        }
}
