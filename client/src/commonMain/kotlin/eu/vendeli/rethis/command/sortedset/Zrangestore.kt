package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRangeStoreCommandCodec
import eu.vendeli.rethis.shared.request.sortedset.ZRangeOption
import eu.vendeli.rethis.shared.request.sortedset.ZRangeStoreLimit
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRangeStore(
    dst: String,
    src: String,
    min: String,
    max: String,
    sortBy: ZRangeOption.Type? = null,
    rev: Boolean? = null,
    limit: ZRangeStoreLimit? = null,
): Long {
    val request = if(cfg.withSlots) {
        ZRangeStoreCommandCodec.encodeWithSlot(charset = cfg.charset, dst = dst, src = src, min = min, max = max, sortBy = sortBy, rev = rev, limit = limit)
    } else {
        ZRangeStoreCommandCodec.encode(charset = cfg.charset, dst = dst, src = src, min = min, max = max, sortBy = sortBy, rev = rev, limit = limit)
    }
    return ZRangeStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
