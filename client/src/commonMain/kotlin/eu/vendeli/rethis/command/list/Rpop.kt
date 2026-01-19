package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.RPopCommandCodec
import eu.vendeli.rethis.codecs.list.RPopCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.rPopCount(key: String, count: Long? = null): List<String> {
    val request = if (cfg.withSlots) {
        RPopCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        RPopCountCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return RPopCountCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.rPop(key: String): String? {
    val request = if (cfg.withSlots) {
        RPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        RPopCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return RPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
