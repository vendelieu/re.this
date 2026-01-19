package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.stream.XDelCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xDel(key: String, vararg id: String): Long {
    val request = if (cfg.withSlots) {
        XDelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, id = id)
    } else {
        XDelCommandCodec.encode(charset = cfg.charset, key = key, id = id)
    }
    return XDelCommandCodec.decode(topology.handle(request), cfg.charset)
}
