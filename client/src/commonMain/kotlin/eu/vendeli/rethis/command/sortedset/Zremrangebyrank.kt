package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRemRangeByRankCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRemRangeByRank(
    key: String,
    start: Long,
    stop: Long,
): Long {
    val request = if (cfg.withSlots) {
        ZRemRangeByRankCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, stop = stop)
    } else {
        ZRemRangeByRankCommandCodec.encode(charset = cfg.charset, key = key, start = start, stop = stop)
    }
    return ZRemRangeByRankCommandCodec.decode(topology.handle(request), cfg.charset)
}
