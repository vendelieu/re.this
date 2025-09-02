package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelConfigSetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelConfigSet(name: String, `value`: String): Boolean {
    val request = if(cfg.withSlots) {
        SentinelConfigSetCommandCodec.encodeWithSlot(charset = cfg.charset, name = name, value = value)
    } else {
        SentinelConfigSetCommandCodec.encode(charset = cfg.charset, name = name, value = value)
    }
    return SentinelConfigSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
