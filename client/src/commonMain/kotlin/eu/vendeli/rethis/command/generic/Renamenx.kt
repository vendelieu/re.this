package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.RenameNxCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.renameNx(key: String, newkey: String): Boolean {
    val request = if (cfg.withSlots) {
        RenameNxCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, newkey = newkey)
    } else {
        RenameNxCommandCodec.encode(charset = cfg.charset, key = key, newkey = newkey)
    }
    return RenameNxCommandCodec.decode(topology.handle(request), cfg.charset)
}
