package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZInterCardCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zInterCard(vararg key: String, limit: Long? = null): Long {
    val request = if (cfg.withSlots) {
        ZInterCardCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, limit = limit)
    } else {
        ZInterCardCommandCodec.encode(charset = cfg.charset, key = key, limit = limit)
    }
    return ZInterCardCommandCodec.decode(topology.handle(request), cfg.charset)
}
