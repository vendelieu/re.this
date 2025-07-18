package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PubSubNumPatCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long

public suspend fun ReThis.pubSubNumPat(): Long {
    val request = if(cfg.withSlots) {
        PubSubNumPatCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        PubSubNumPatCommandCodec.encode(charset = cfg.charset, )
    }
    return PubSubNumPatCommandCodec.decode(topology.handle(request), cfg.charset)
}
