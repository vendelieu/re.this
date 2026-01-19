package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRemRangeByLexCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRemRangeByLex(
    key: String,
    min: String,
    max: String,
): Long {
    val request = if (cfg.withSlots) {
        ZRemRangeByLexCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, min = min, max = max)
    } else {
        ZRemRangeByLexCommandCodec.encode(charset = cfg.charset, key = key, min = min, max = max)
    }
    return ZRemRangeByLexCommandCodec.decode(topology.handle(request), cfg.charset)
}
