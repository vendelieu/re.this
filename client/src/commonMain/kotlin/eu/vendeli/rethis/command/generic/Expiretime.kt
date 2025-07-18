package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ExpireTimeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.expireTime(key: String): Long {
    val request = if(cfg.withSlots) {
        ExpireTimeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ExpireTimeCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ExpireTimeCommandCodec.decode(topology.handle(request), cfg.charset)
}
