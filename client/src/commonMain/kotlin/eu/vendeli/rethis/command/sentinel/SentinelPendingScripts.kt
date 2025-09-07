package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelPendingScriptsCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelPendingScripts(): List<RType> {
    val request = if(cfg.withSlots) {
        SentinelPendingScriptsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SentinelPendingScriptsCommandCodec.encode(charset = cfg.charset, )
    }
    return SentinelPendingScriptsCommandCodec.decode(topology.handle(request), cfg.charset)
}
