package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelResetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.sentinelReset(pattern: String): Boolean {
    val request = if(cfg.withSlots) {
        SentinelResetCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        SentinelResetCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return SentinelResetCommandCodec.decode(topology.handle(request), cfg.charset)
}
