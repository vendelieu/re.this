package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.RPushxCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.rPushx(key: String, vararg element: String): Long {
    val request = if(cfg.withSlots) {
        RPushxCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushxCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushxCommandCodec.decode(topology.handle(request), cfg.charset)
}
