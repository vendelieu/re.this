package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelFlushConfigCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelFlushConfig(): Boolean {
    val request = if (cfg.withSlots) {
        SentinelFlushConfigCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        SentinelFlushConfigCommandCodec.encode(charset = cfg.charset)
    }
    return SentinelFlushConfigCommandCodec.decode(topology.handle(request), cfg.charset)
}
