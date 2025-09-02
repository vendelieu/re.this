package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZDiffStoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zDiffStore(destination: String, vararg key: String): Long {
    val request = if(cfg.withSlots) {
        ZDiffStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, key = key)
    } else {
        ZDiffStoreCommandCodec.encode(charset = cfg.charset, destination = destination, key = key)
    }
    return ZDiffStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
