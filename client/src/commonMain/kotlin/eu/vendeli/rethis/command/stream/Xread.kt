package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XReadCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xRead(
    key: List<String>,
    id: List<String>,
    count: Long? = null,
    milliseconds: Long? = null,
): Map<String, RType>? {
    val request = if (cfg.withSlots) {
        XReadCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            id = id,
            count = count,
            milliseconds = milliseconds,
        )
    } else {
        XReadCommandCodec.encode(charset = cfg.charset, key = key, id = id, count = count, milliseconds = milliseconds)
    }
    return XReadCommandCodec.decode(topology.handle(request), cfg.charset)
}
