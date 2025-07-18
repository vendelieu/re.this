package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HPttlCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.hPttl(key: String, vararg `field`: String): List<Long> {
    val request = if(cfg.withSlots) {
        HPttlCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HPttlCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HPttlCommandCodec.decode(topology.handle(request), cfg.charset)
}
