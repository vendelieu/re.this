package eu.vendeli.rethis.command.transaction

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.transaction.DiscardCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.discard(): Boolean {
    val request = if(cfg.withSlots) {
        DiscardCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        DiscardCommandCodec.encode(charset = cfg.charset, )
    }
    return DiscardCommandCodec.decode(topology.handle(request), cfg.charset)
}
