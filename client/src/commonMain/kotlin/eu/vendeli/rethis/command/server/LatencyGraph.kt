package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.LatencyGraphCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String

public suspend fun ReThis.latencyGraph(event: String): String {
    val request = if(cfg.withSlots) {
        LatencyGraphCommandCodec.encodeWithSlot(charset = cfg.charset, event = event)
    } else {
        LatencyGraphCommandCodec.encode(charset = cfg.charset, event = event)
    }
    return LatencyGraphCommandCodec.decode(topology.handle(request), cfg.charset)
}
