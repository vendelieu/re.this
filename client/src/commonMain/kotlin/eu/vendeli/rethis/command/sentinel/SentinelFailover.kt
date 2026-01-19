package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelFailoverCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelFailover(masterName: String): Boolean {
    val request = if (cfg.withSlots) {
        SentinelFailoverCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelFailoverCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelFailoverCommandCodec.decode(topology.handle(request), cfg.charset)
}
