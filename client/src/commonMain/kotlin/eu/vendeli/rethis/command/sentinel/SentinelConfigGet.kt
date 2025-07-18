package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelConfigGetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Map

public suspend fun ReThis.sentinelConfigGet(pattern: String): Map<String, String> {
    val request = if(cfg.withSlots) {
        SentinelConfigGetCommandCodec.encodeWithSlot(charset = cfg.charset, pattern = pattern)
    } else {
        SentinelConfigGetCommandCodec.encode(charset = cfg.charset, pattern = pattern)
    }
    return SentinelConfigGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
