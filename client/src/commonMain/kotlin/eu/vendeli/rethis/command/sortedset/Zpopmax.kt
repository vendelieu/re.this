package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZPopMaxCommandCodec
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zPopMax(key: String, count: Long? = null): List<MPopResult> {
    val request = if(cfg.withSlots) {
        ZPopMaxCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        ZPopMaxCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return ZPopMaxCommandCodec.decode(topology.handle(request), cfg.charset)
}
