package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelMyIdCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String

public suspend fun ReThis.sentinelMyId(): String {
    val request = if(cfg.withSlots) {
        SentinelMyIdCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SentinelMyIdCommandCodec.encode(charset = cfg.charset, )
    }
    return SentinelMyIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
