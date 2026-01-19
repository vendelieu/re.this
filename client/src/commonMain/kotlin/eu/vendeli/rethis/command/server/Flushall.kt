package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.FlushAllCommandCodec
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.flushAll(flushType: FlushType? = null): Boolean {
    val request = if (cfg.withSlots) {
        FlushAllCommandCodec.encodeWithSlot(charset = cfg.charset, flushType = flushType)
    } else {
        FlushAllCommandCodec.encode(charset = cfg.charset, flushType = flushType)
    }
    return FlushAllCommandCodec.decode(topology.handle(request), cfg.charset)
}
