package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MemoryUsageCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.memoryUsage(key: String, count: Long? = null): Long? {
    val request = if (cfg.withSlots) {
        MemoryUsageCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        MemoryUsageCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return MemoryUsageCommandCodec.decode(topology.handle(request), cfg.charset)
}
