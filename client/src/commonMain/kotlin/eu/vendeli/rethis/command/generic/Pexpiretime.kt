package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.PExpireTimeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pExpireTime(key: String): Long {
    val request = if(cfg.withSlots) {
        PExpireTimeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        PExpireTimeCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return PExpireTimeCommandCodec.decode(topology.handle(request), cfg.charset)
}
