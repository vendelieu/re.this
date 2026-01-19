package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelRemoveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelRemove(masterName: String): Boolean {
    val request = if (cfg.withSlots) {
        SentinelRemoveCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelRemoveCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelRemoveCommandCodec.decode(topology.handle(request), cfg.charset)
}
