package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.RPushCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.rPush(key: String, vararg element: String): Long {
    val request = if(cfg.withSlots) {
        RPushCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushCommandCodec.decode(topology.handle(request), cfg.charset)
}
