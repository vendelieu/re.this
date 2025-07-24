package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZLexCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zLexCount(
    key: String,
    min: String,
    max: String,
): Long {
    val request = if(cfg.withSlots) {
        ZLexCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, min = min, max = max)
    } else {
        ZLexCountCommandCodec.encode(charset = cfg.charset, key = key, min = min, max = max)
    }
    return ZLexCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
