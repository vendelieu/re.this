package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.SelectCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.select(index: Long): Boolean {
    val request = if(cfg.withSlots) {
        SelectCommandCodec.encodeWithSlot(charset = cfg.charset, index = index)
    } else {
        SelectCommandCodec.encode(charset = cfg.charset, index = index)
    }
    return SelectCommandCodec.decode(topology.handle(request), cfg.charset)
}
