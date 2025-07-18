package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HExpireTimeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.hExpireTime(key: String, vararg `field`: String): List<Long> {
    val request = if(cfg.withSlots) {
        HExpireTimeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HExpireTimeCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HExpireTimeCommandCodec.decode(topology.handle(request), cfg.charset)
}
