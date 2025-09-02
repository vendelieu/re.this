package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.server.SlowLogGetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.slowLogGet(count: Long? = null): List<RType> {
    val request = if(cfg.withSlots) {
        SlowLogGetCommandCodec.encodeWithSlot(charset = cfg.charset, count = count)
    } else {
        SlowLogGetCommandCodec.encode(charset = cfg.charset, count = count)
    }
    return SlowLogGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
