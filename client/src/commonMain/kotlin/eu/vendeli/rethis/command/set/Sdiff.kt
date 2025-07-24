package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SDiffCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sDiff(vararg key: String): Set<String> {
    val request = if(cfg.withSlots) {
        SDiffCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SDiffCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SDiffCommandCodec.decode(topology.handle(request), cfg.charset)
}
