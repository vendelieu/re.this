package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.SwapDbCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long

public suspend fun ReThis.swapDb(index1: Long, index2: Long): Boolean {
    val request = if(cfg.withSlots) {
        SwapDbCommandCodec.encodeWithSlot(charset = cfg.charset, index1 = index1, index2 = index2)
    } else {
        SwapDbCommandCodec.encode(charset = cfg.charset, index1 = index1, index2 = index2)
    }
    return SwapDbCommandCodec.decode(topology.handle(request), cfg.charset)
}
