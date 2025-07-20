package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.topology.StandaloneTopologyManager
import eu.vendeli.rethis.types.common.RConnection

private suspend inline fun RConnection.doRTypeRequest(client: ReThis, payload: List<CommandRequest>): List<RType> {
    return ArrayRTypeDecoder.decode(doRequest(payload.map { it.buffer }), client.cfg.charset)
}

internal suspend fun ReThis.handlePipelinedRequests(
    pipelined: List<CommandRequest>,
    ctxConn: RConnection?,
): List<RType> {
    if (topology is StandaloneTopologyManager) {
        return ctxConn?.doRTypeRequest(this, pipelined) ?: topology.provider.withConnection { conn ->
            ArrayRTypeDecoder.decode(conn.doRequest(pipelined.map { it.buffer }), cfg.charset)
        }
    }

    val preparedRequests = pipelined.groupBy { it.computedSlot }
    val responses = mutableListOf<RType>()

    preparedRequests.forEach { r ->
        if (ctxConn != null) {
            ctxConn.doRTypeRequest(this, r.value)
        } else topology.route(r.value.first()).withConnection { conn ->
            conn.doRTypeRequest(this, r.value)
        }.also {
            responses.addAll(it)
        }
    }

    return responses
}
