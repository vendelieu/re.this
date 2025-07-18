package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PubSubChannelsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.pubSubChannels(pattern: String? = null): List<String> {
    val request = if(cfg.withSlots) {
        PubSubChannelsCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        PubSubChannelsCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return PubSubChannelsCommandCodec.decode(topology.handle(request), cfg.charset)
}
