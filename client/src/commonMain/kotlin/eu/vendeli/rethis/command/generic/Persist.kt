package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.PersistCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.persist(key: String): Boolean {
    val request = if(cfg.withSlots) {
        PersistCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        PersistCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return PersistCommandCodec.decode(topology.handle(request), cfg.charset)
}
