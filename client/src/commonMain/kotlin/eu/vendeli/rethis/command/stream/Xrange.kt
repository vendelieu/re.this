package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.stream.XRangeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xRange(
    key: String,
    start: String,
    end: String,
    count: Long? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        XRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, end = end, count = count)
    } else {
        XRangeCommandCodec.encode(charset = cfg.charset, key = key, start = start, end = end, count = count)
    }
    return XRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
