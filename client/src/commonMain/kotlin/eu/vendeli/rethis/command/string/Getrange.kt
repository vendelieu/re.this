package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetRangeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.getRange(
    key: String,
    start: Long,
    end: Long,
): String {
    val request = if(cfg.withSlots) {
        GetRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, end = end)
    } else {
        GetRangeCommandCodec.encode(charset = cfg.charset, key = key, start = start, end = end)
    }
    return GetRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
