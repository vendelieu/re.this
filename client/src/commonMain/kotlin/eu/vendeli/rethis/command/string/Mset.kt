package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.MSetCommandCodec
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.mSet(vararg `data`: KeyValue): Boolean {
    val request = if(cfg.withSlots) {
        MSetCommandCodec.encodeWithSlot(charset = cfg.charset, data = data)
    } else {
        MSetCommandCodec.encode(charset = cfg.charset, data = data)
    }
    return MSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
