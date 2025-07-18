package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelCkQuorumCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.sentinelCkQuorum(masterName: String): Boolean {
    val request = if(cfg.withSlots) {
        SentinelCkQuorumCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelCkQuorumCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelCkQuorumCommandCodec.decode(topology.handle(request), cfg.charset)
}
