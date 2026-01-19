package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ObjectIdleTimeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.objectIdleTime(key: String): Long? {
    val request = if (cfg.withSlots) {
        ObjectIdleTimeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ObjectIdleTimeCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ObjectIdleTimeCommandCodec.decode(topology.handle(request), cfg.charset)
}
