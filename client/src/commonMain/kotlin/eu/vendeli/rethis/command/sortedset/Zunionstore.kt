package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZUnionStoreCommandCodec
import eu.vendeli.rethis.shared.request.sortedset.ZAggregate
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zUnionStore(
    destination: String,
    vararg key: String,
    weight: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long {
    val request = if(cfg.withSlots) {
        ZUnionStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key, weight = weight, aggregate = aggregate)
    } else {
        ZUnionStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key, weight = weight, aggregate = aggregate)
    }
    return ZUnionStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
