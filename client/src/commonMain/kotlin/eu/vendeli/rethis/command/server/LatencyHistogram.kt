package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.LatencyHistogramCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.latencyHistogram(vararg command: String): Map<String, RType> {
    val request = if (cfg.withSlots) {
        LatencyHistogramCommandCodec.encodeWithSlot(charset = cfg.charset, command = command)
    } else {
        LatencyHistogramCommandCodec.encode(charset = cfg.charset, command = command)
    }
    return LatencyHistogramCommandCodec.decode(topology.handle(request), cfg.charset)
}
