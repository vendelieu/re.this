package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.stream.XInfoStreamCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xInfoStream(
    key: String,
    full: Boolean? = null,
    count: Long? = null,
): Map<String, RType> {
    val request = if(cfg.withSlots) {
        XInfoStreamCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, full = full, count = count)
    } else {
        XInfoStreamCommandCodec.encode(charset = cfg.charset, key = key, full = full, count = count)
    }
    return XInfoStreamCommandCodec.decode(topology.handle(request), cfg.charset)
}
