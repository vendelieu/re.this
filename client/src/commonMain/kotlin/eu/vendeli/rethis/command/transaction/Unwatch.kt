package eu.vendeli.rethis.command.transaction

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.transaction.UnwatchCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.unwatch(): Boolean {
    val request = if(cfg.withSlots) {
        UnwatchCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        UnwatchCommandCodec.encode(charset = cfg.charset, )
    }
    return UnwatchCommandCodec.decode(topology.handle(request), cfg.charset)
}
