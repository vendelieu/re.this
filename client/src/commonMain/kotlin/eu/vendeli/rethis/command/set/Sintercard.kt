package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SInterCardCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.sInterCard(vararg key: String, limit: Long? = null): Long {
    val request = if(cfg.withSlots) {
        SInterCardCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, limit = limit)
    } else {
        SInterCardCommandCodec.encode(charset = cfg.charset, key = key, limit = limit)
    }
    return SInterCardCommandCodec.decode(topology.handle(request), cfg.charset)
}
