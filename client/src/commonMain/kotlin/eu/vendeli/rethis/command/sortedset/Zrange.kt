package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption
import eu.vendeli.rethis.codecs.sortedset.ZRangeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.zRange(
    key: String,
    start: String,
    stop: String,
    sortBy: ZRangeOption.Type? = null,
    rev: Boolean? = null,
    limit: ZRangeOption.Limit? = null,
    withScores: Boolean? = null,
): List<String> {
    val request = if(cfg.withSlots) {
        ZRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, stop = stop, sortBy = sortBy, rev = rev, limit = limit, withScores = withScores)
    } else {
        ZRangeCommandCodec.encode(charset = cfg.charset, key = key, start = start, stop = stop, sortBy = sortBy, rev = rev, limit = limit, withScores = withScores)
    }
    return ZRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
