package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SDiffStoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sDiffStore(destination: String, vararg key: String): Long {
    val request = if(cfg.withSlots) {
        SDiffStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key)
    } else {
        SDiffStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key)
    }
    return SDiffStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
