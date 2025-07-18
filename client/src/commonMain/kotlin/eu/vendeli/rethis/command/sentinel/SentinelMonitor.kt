package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelMonitorCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Int
import kotlin.String

public suspend fun ReThis.sentinelMonitor(
    masterName: String,
    ip: String,
    port: Int,
    quorum: Int,
): Boolean {
    val request = if(cfg.withSlots) {
        SentinelMonitorCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName, ip = ip, port = port, quorum = quorum)
    } else {
        SentinelMonitorCommandCodec.encode(charset = cfg.charset, masterName = masterName, ip = ip, port = port, quorum = quorum)
    }
    return SentinelMonitorCommandCodec.decode(topology.handle(request), cfg.charset)
}
