package eu.vendeli.rethis.command.transaction

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.transaction.MultiCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.multi(): Boolean {
    val request = if(cfg.withSlots) {
        MultiCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        MultiCommandCodec.encode(charset = cfg.charset, )
    }
    return MultiCommandCodec.decode(topology.handle(request), cfg.charset)
}
