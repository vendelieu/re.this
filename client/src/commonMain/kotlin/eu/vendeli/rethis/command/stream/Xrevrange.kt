package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XRevRangeCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xRevRange(
    key: String,
    end: String,
    start: String,
    count: Long? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        XRevRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, end = end, start = start, count = count)
    } else {
        XRevRangeCommandCodec.encode(charset = cfg.charset, key = key, end = end, start = start, count = count)
    }
    return XRevRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
