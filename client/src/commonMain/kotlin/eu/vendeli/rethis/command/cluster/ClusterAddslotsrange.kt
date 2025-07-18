package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.codecs.cluster.ClusterAddSlotsRangeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.clusterAddSlotsRange(vararg range: SlotRange): Boolean {
    val request = if(cfg.withSlots) {
        ClusterAddSlotsRangeCommandCodec.encodeWithSlot(charset = cfg.charset, range = range)
    } else {
        ClusterAddSlotsRangeCommandCodec.encode(charset = cfg.charset, range = range)
    }
    return ClusterAddSlotsRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
