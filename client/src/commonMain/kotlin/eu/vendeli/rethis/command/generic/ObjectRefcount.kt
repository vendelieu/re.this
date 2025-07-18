package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ObjectRefCountCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.objectRefCount(key: String): Long? {
    val request = if(cfg.withSlots) {
        ObjectRefCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ObjectRefCountCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ObjectRefCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
