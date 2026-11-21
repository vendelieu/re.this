package eu.vendeli.rethis.utils

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.topology.CLUSTER_SLOT_COUNT
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.common.encode
import io.ktor.utils.io.core.toByteArray

/**
 * Marks the request with the cluster slot of [channel]: shard channels are hashed like keys, so
 * SPUBLISH / SSUBSCRIBE must reach the node owning that slot. Nothing is marked unless [ReThisConfiguration.withSlots].
 */
internal fun CommandRequest.withShardChannelSlot(channel: String, cfg: ReThisConfiguration): CommandRequest =
    if (cfg.withSlots) withSlot(CRC16.slot(channel.toByteArray(cfg.charset)) % CLUSTER_SLOT_COUNT) else this

/** The subscribe request used to pick the node: shard targets carry the slot of their channel. */
internal fun SubscribeTarget.routingRequest(cfg: ReThisConfiguration): CommandRequest {
    val request = encode(cfg.charset)
    return if (this is SubscribeTarget.Shard) request.withShardChannelSlot(name, cfg) else request
}
