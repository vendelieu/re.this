package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HValsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hVals(key: String): List<String> {
    val request = if (cfg.withSlots) {
        HValsCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HValsCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HValsCommandCodec.decode(topology.handle(request), cfg.charset)
}
