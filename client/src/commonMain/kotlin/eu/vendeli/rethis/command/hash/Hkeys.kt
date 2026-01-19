package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HKeysCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hKeys(key: String): List<String> {
    val request = if (cfg.withSlots) {
        HKeysCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HKeysCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HKeysCommandCodec.decode(topology.handle(request), cfg.charset)
}
