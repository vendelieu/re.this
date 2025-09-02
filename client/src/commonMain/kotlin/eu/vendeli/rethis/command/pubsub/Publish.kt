package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.PublishCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.publish(channel: String, message: String): Long {
    val request = if(cfg.withSlots) {
        PublishCommandCodec.encodeWithSlot(charset = cfg.charset, channel = channel, message = message)
    } else {
        PublishCommandCodec.encode(charset = cfg.charset, channel = channel, message = message)
    }
    return PublishCommandCodec.decode(topology.handle(request), cfg.charset)
}
