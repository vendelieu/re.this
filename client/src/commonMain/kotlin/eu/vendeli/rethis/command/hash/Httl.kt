package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HTtlCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hTtl(key: String, vararg `field`: String): List<Long> {
    val request = if (cfg.withSlots) {
        HTtlCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HTtlCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HTtlCommandCodec.decode(topology.handle(request), cfg.charset)
}
