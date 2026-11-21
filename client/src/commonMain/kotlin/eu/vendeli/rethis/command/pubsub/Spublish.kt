package eu.vendeli.rethis.command.pubsub

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.pubsub.SPublishCommandCodec
import eu.vendeli.rethis.topology.handle
import eu.vendeli.rethis.utils.withShardChannelSlot

/**
 * Publishes [message] to the shard channel [shardchannel]. In a cluster the request goes to the node owning the
 * slot of the channel, which is hashed like a key.
 */
public suspend fun ReThis.sPublish(shardchannel: String, message: String): Long {
    val request = SPublishCommandCodec.encode(charset = cfg.charset, shardchannel = shardchannel, message = message)
    return SPublishCommandCodec.decode(topology.handle(request.withShardChannelSlot(shardchannel, cfg)), cfg.charset)
}
