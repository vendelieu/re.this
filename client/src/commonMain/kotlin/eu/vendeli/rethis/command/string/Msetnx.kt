package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.codecs.string.MSetNxCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.mSetNx(vararg `data`: KeyValue): Boolean {
    val request = if(cfg.withSlots) {
        MSetNxCommandCodec.encodeWithSlot(charset = cfg.charset, data = data)
    } else {
        MSetNxCommandCodec.encode(charset = cfg.charset, data = data)
    }
    return MSetNxCommandCodec.decode(topology.handle(request), cfg.charset)
}
