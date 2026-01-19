package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRemRangeByScoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRemRangeByScore(
    key: String,
    min: Double,
    max: Double,
): Long {
    val request = if (cfg.withSlots) {
        ZRemRangeByScoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, min = min, max = max)
    } else {
        ZRemRangeByScoreCommandCodec.encode(charset = cfg.charset, key = key, min = min, max = max)
    }
    return ZRemRangeByScoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
