package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PUnsubscribeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pUnsubscribe(vararg pattern: String) {
    val request = if (cfg.withSlots) {
        PUnsubscribeCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        PUnsubscribeCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return PUnsubscribeCommandCodec.decode(topology.handle(request), cfg.charset)
}
