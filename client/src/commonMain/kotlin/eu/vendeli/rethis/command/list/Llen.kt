package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LLenCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.lLen(key: String): Long {
    val request = if(cfg.withSlots) {
        LLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        LLenCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return LLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
