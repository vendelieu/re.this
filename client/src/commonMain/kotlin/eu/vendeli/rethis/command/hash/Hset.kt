package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.common.FieldValue
import eu.vendeli.rethis.codecs.hash.HSetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.hSet(key: String, vararg `data`: FieldValue): Long {
    val request = if(cfg.withSlots) {
        HSetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, data = data)
    } else {
        HSetCommandCodec.encode(charset = cfg.charset, key = key, data = data)
    }
    return HSetCommandCodec.decode(topology.handle(request), cfg.charset)
}
