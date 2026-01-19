package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XLenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xLen(key: String): Long {
    val request = if (cfg.withSlots) {
        XLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        XLenCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return XLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
