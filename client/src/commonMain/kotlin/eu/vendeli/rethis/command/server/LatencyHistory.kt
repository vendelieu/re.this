package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.server.LatencyHistoryCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.latencyHistory(event: String): List<RType> {
    val request = if(cfg.withSlots) {
        LatencyHistoryCommandCodec.encodeWithSlot(charset = cfg.charset, event = event)
    } else {
        LatencyHistoryCommandCodec.encode(charset = cfg.charset, event = event)
    }
    return LatencyHistoryCommandCodec.decode(topology.handle(request), cfg.charset)
}
