package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SInterStoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sInterStore(destination: String, vararg key: String): Long {
    val request = if(cfg.withSlots) {
        SInterStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key)
    } else {
        SInterStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key)
    }
    return SInterStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
