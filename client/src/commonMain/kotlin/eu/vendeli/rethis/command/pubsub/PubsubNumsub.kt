package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PubSubNumSubCommandCodec
import eu.vendeli.rethis.shared.response.common.PubSubNumEntry
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pubSubNumSub(vararg channel: String): List<PubSubNumEntry> {
    val request = if (cfg.withSlots) {
        PubSubNumSubCommandCodec.encodeWithSlot(charset = cfg.charset, channel = channel)
    } else {
        PubSubNumSubCommandCodec.encode(charset = cfg.charset, channel = channel)
    }
    return PubSubNumSubCommandCodec.decode(topology.handle(request), cfg.charset)
}
