package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PubSubShardChannelsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.pubSubShardChannels(pattern: String? = null): List<String> {
    val request = if(cfg.withSlots) {
        PubSubShardChannelsCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        PubSubShardChannelsCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return PubSubShardChannelsCommandCodec.decode(topology.handle(request), cfg.charset)
}
