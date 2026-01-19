package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LPushxCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lPushx(key: String, vararg element: String): Long {
    val request = if (cfg.withSlots) {
        LPushxCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        LPushxCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return LPushxCommandCodec.decode(topology.handle(request), cfg.charset)
}
