package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HLenCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.hLen(key: String): Long {
    val request = if(cfg.withSlots) {
        HLenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HLenCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
