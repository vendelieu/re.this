package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelInfoCacheCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelInfoCache(): Map<String, String> {
    val request = if (cfg.withSlots) {
        SentinelInfoCacheCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        SentinelInfoCacheCommandCodec.encode(charset = cfg.charset)
    }
    return SentinelInfoCacheCommandCodec.decode(topology.handle(request), cfg.charset)
}
