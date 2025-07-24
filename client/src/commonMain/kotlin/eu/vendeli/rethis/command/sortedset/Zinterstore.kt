package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZAggregate
import eu.vendeli.rethis.codecs.sortedset.ZInterStoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zInterStore(
    destination: String,
    vararg key: String,
    weight: List<Long>,
    aggregate: ZAggregate? = null,
): Long {
    val request = if(cfg.withSlots) {
        ZInterStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key, weight = weight, aggregate = aggregate)
    } else {
        ZInterStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key, weight = weight, aggregate = aggregate)
    }
    return ZInterStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
