package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.RenameCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.rename(key: String, newkey: String): Boolean {
    val request = if (cfg.withSlots) {
        RenameCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, newkey = newkey)
    } else {
        RenameCommandCodec.encode(charset = cfg.charset, key = key, newkey = newkey)
    }
    return RenameCommandCodec.decode(topology.handle(request), cfg.charset)
}
