package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.UnsubscribeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.unsubscribe(vararg channel: String) {
    val request = if(cfg.withSlots) {
        UnsubscribeCommandCodec.encodeWithSlot(charset = cfg.charset, channel = channel)
    } else {
        UnsubscribeCommandCodec.encode(charset = cfg.charset, channel = channel)
    }
    channel.forEach { channel -> subscriptions.unsubscribe(channel)}
    return UnsubscribeCommandCodec.decode(topology.handle(request), cfg.charset)
}
