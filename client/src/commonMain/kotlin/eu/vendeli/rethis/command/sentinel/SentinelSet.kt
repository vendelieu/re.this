package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.codecs.sentinel.SentinelSetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelSet(name: String, vararg optionValue: FieldValue): Boolean {
    val request = if(cfg.withSlots) {
        SentinelSetCommandCodec.encodeWithSlot(charset = cfg.charset, name = name, optionValue = optionValue)
    } else {
        SentinelSetCommandCodec.encode(charset = cfg.charset, name = name, optionValue = optionValue)
    }
    return SentinelSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
