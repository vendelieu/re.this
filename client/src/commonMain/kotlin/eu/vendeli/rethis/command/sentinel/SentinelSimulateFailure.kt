package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.sentinel.SentinelSimulateFailureMode
import eu.vendeli.rethis.codecs.sentinel.SentinelSimulateFailureCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelSimulateFailure(type: SentinelSimulateFailureMode): Boolean {
    val request = if(cfg.withSlots) {
        SentinelSimulateFailureCommandCodec.encodeWithSlot(charset = cfg.charset, type = type)
    } else {
        SentinelSimulateFailureCommandCodec.encode(charset = cfg.charset, type = type)
    }
    return SentinelSimulateFailureCommandCodec.decode(topology.handle(request), cfg.charset)
}
