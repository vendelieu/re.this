package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SCardCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sCard(key: String): Long {
    val request = if(cfg.withSlots) {
        SCardCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SCardCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SCardCommandCodec.decode(topology.handle(request), cfg.charset)
}
