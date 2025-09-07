package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.BlPopCommandCodec
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.blPop(vararg key: String, timeout: Double): PopResult? {
    val request = if(cfg.withSlots) {
        BlPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, timeout = timeout)
    } else {
        BlPopCommandCodec.encode(charset = cfg.charset, key = key, timeout = timeout)
    }
    return BlPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
