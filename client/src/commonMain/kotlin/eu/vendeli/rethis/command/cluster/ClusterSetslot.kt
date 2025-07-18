package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption
import eu.vendeli.rethis.codecs.cluster.ClusterSetSlotCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long

public suspend fun ReThis.clusterSetSlot(slot: Long, subcommand: ClusterSetSlotOption): Boolean {
    val request = if(cfg.withSlots) {
        ClusterSetSlotCommandCodec.encodeWithSlot(charset = cfg.charset, slot = slot, subcommand = subcommand)
    } else {
        ClusterSetSlotCommandCodec.encode(charset = cfg.charset, slot = slot, subcommand = subcommand)
    }
    return ClusterSetSlotCommandCodec.decode(topology.handle(request), cfg.charset)
}
