package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HGetBACommandCodec
import eu.vendeli.rethis.codecs.hash.HGetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hGetBA(key: String, `field`: String): ByteArray? {
    val request = if(cfg.withSlots) {
        HGetBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HGetBACommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HGetBACommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.hGet(key: String, `field`: String): String? {
    val request = if(cfg.withSlots) {
        HGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HGetCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
