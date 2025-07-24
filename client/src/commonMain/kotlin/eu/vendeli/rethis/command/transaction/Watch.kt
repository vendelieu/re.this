package eu.vendeli.rethis.command.transaction

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.transaction.WatchCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.watch(vararg key: String): Boolean {
    val request = if(cfg.withSlots) {
        WatchCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        WatchCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return WatchCommandCodec.decode(topology.handle(request), cfg.charset)
}
