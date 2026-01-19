package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LPushCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lPush(key: String, vararg element: String): Long {
    val request = if (cfg.withSlots) {
        LPushCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        LPushCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return LPushCommandCodec.decode(topology.handle(request), cfg.charset)
}
