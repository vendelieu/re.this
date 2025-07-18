package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.TimeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.collections.List

public suspend fun ReThis.time(): List<Long> {
    val request = if(cfg.withSlots) {
        TimeCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        TimeCommandCodec.encode(charset = cfg.charset, )
    }
    return TimeCommandCodec.decode(topology.handle(request), cfg.charset)
}
