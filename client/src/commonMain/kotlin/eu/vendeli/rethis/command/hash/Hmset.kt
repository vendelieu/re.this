package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.FieldValue
import eu.vendeli.rethis.codecs.hash.HMSetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hMSet(key: String, vararg `data`: FieldValue): Boolean {
    val request = if(cfg.withSlots) {
        HMSetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, data = data)
    } else {
        HMSetCommandCodec.encode(charset = cfg.charset, key = key, data = data)
    }
    return HMSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
