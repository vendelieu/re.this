package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.codecs.pubsub.PubSubShardNumSubCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pubSubShardNumSub(vararg shardchannel: String): List<PubSubNumEntry> {
    val request = if(cfg.withSlots) {
        PubSubShardNumSubCommandCodec.encodeWithSlot(charset = cfg.charset, shardchannel = shardchannel)
    } else {
        PubSubShardNumSubCommandCodec.encode(charset = cfg.charset, shardchannel = shardchannel)
    }
    return PubSubShardNumSubCommandCodec.decode(topology.handle(request), cfg.charset)
}
