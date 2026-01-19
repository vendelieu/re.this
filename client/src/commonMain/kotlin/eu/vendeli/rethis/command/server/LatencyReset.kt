package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.LatencyResetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.latencyReset(vararg event: String): Long {
    val request = if (cfg.withSlots) {
        LatencyResetCommandCodec.encodeWithSlot(charset = cfg.charset, event = event)
    } else {
        LatencyResetCommandCodec.encode(charset = cfg.charset, event = event)
    }
    return LatencyResetCommandCodec.decode(topology.handle(request), cfg.charset)
}
