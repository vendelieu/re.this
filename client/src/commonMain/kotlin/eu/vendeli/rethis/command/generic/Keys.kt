package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.KeysCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.keys(pattern: String): List<String> {
    val request = if (cfg.withSlots) {
        KeysCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        KeysCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return KeysCommandCodec.decode(topology.handle(request), cfg.charset)
}
