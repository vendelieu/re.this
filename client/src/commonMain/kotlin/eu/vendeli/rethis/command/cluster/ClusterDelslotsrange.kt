package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.codecs.cluster.ClusterDelSlotsRangeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.clusterDelSlotsRange(vararg range: SlotRange): Boolean {
    val request = if(cfg.withSlots) {
        ClusterDelSlotsRangeCommandCodec.encodeWithSlot(charset = cfg.charset, range = range)
    } else {
        ClusterDelSlotsRangeCommandCodec.encode(charset = cfg.charset, range = range)
    }
    return ClusterDelSlotsRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
