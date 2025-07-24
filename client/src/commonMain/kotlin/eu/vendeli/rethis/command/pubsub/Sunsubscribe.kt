package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.SUnsubscribeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sUnsubscribe(vararg shardchannel: String) {
    val request = if(cfg.withSlots) {
        SUnsubscribeCommandCodec.encodeWithSlot(charset = cfg.charset, shardchannel = shardchannel)
    } else {
        SUnsubscribeCommandCodec.encode(charset = cfg.charset, shardchannel = shardchannel)
    }
    return SUnsubscribeCommandCodec.decode(topology.handle(request), cfg.charset)
}
