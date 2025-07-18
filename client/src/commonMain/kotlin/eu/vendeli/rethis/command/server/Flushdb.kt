package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.FlushType
import eu.vendeli.rethis.codecs.server.FlushDbCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.flushDb(flushType: FlushType? = null): Boolean {
    val request = if(cfg.withSlots) {
        FlushDbCommandCodec.encodeWithSlot(charset = cfg.charset, flushType = flushType)
    } else {
        FlushDbCommandCodec.encode(charset = cfg.charset, flushType = flushType)
    }
    return FlushDbCommandCodec.decode(topology.handle(request), cfg.charset)
}
