package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.StandaloneTopologyManager
import eu.vendeli.rethis.types.common.RConnection

internal suspend fun ReThis.handlePipelinedRequests(
    pipelined: List<CommandRequest>,
    ctxConn: RConnection?,
): List<RType> {
    if (topology is StandaloneTopologyManager) {
        return ctxConn?.doRTypeRequest(this, pipelined) ?: topology.provider.withConnection { conn ->
            conn.doRTypeRequest(this, pipelined)
        }
    }

    val preparedRequests = pipelined.groupBy { it.computedSlot }
    val responses = mutableListOf<RType>()

    preparedRequests.forEach { r ->
        val response = ctxConn?.doRTypeRequest(this, r.value)
            ?: topology.route(r.value.first()).withConnection { conn ->
                conn.doRTypeRequest(this, r.value)
            }

        responses.addAll(response)
    }

    return responses
}

private suspend inline fun RConnection.doRTypeRequest(client: ReThis, payload: List<CommandRequest>): List<RType> {
    val payloadBuffer = payload.map { it.buffer }
    val request = doBatchRequest(payloadBuffer)
    return buildList {
        repeat(payloadBuffer.size) {
            add(RTypeDecoder.decode(request, client.cfg.charset))
        }
    }
}
