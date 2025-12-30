package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetDelBACommandCodec
import eu.vendeli.rethis.codecs.string.GetDelCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.getDel(key: String): String? {
    val request = if(cfg.withSlots) {
        GetDelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetDelCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetDelCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.getDelBA(key: String): ByteArray? {
    val request = if(cfg.withSlots) {
        GetDelBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetDelBACommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetDelBACommandCodec.decode(topology.handle(request), cfg.charset)
}
