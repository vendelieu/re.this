package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.UnlinkCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.unlink(vararg key: String): Long {
    val request = if(cfg.withSlots) {
        UnlinkCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        UnlinkCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return UnlinkCommandCodec.decode(topology.handle(request), cfg.charset)
}
