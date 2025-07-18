package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.SPublishCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.sPublish(shardchannel: String, message: String): Long {
    val request = if(cfg.withSlots) {
        SPublishCommandCodec.encodeWithSlot(charset = cfg.charset, shardchannel = shardchannel, message = message)
    } else {
        SPublishCommandCodec.encode(charset = cfg.charset, shardchannel = shardchannel, message = message)
    }
    return SPublishCommandCodec.decode(topology.handle(request), cfg.charset)
}
