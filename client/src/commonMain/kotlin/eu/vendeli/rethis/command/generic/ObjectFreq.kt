package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.generic.ObjectFreqCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.objectFreq(key: String): RType? {
    val request = if(cfg.withSlots) {
        ObjectFreqCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ObjectFreqCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ObjectFreqCommandCodec.decode(topology.handle(request), cfg.charset)
}
