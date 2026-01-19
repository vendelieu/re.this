package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZPopMinCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zPopMin(key: String, count: Long? = null): List<RType> {
    val request = if (cfg.withSlots) {
        ZPopMinCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        ZPopMinCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return ZPopMinCommandCodec.decode(topology.handle(request), cfg.charset)
}
