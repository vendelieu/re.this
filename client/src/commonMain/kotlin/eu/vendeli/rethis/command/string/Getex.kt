package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetExBACommandCodec
import eu.vendeli.rethis.codecs.string.GetExCommandCodec
import eu.vendeli.rethis.shared.request.string.GetExOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.getEx(key: String, vararg expiration: GetExOption): String? {
    val request = if(cfg.withSlots) {
        GetExCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, expiration = expiration)
    } else {
        GetExCommandCodec.encode(charset = cfg.charset, key = key, expiration = expiration)
    }
    return GetExCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.getExBA(key: String, vararg expiration: GetExOption): ByteArray? {
    val request = if(cfg.withSlots) {
        GetExBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key, expiration = expiration)
    } else {
        GetExBACommandCodec.encode(charset = cfg.charset, key = key, expiration = expiration)
    }
    return GetExBACommandCodec.decode(topology.handle(request), cfg.charset)
}
