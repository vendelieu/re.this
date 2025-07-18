package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LSetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.lSet(
    key: String,
    index: Long,
    element: String,
): String {
    val request = if(cfg.withSlots) {
        LSetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, index = index, element = element)
    } else {
        LSetCommandCodec.encode(charset = cfg.charset, key = key, index = index, element = element)
    }
    return LSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
