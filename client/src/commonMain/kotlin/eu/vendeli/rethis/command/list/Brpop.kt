package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.codecs.list.BrPopCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.brPop(vararg key: String, timeout: Double): PopResult? {
    val request = if(cfg.withSlots) {
        BrPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, timeout = timeout)
    } else {
        BrPopCommandCodec.encode(charset = cfg.charset, key = key, timeout = timeout)
    }
    return BrPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
