package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.RPushBACommandCodec
import eu.vendeli.rethis.codecs.list.RPushCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.rPushBA(key: String, vararg element: ByteArray): Long {
    val request = if(cfg.withSlots) {
        RPushBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushBACommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushBACommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.rPush(key: String, vararg element: String): Long {
    val request = if(cfg.withSlots) {
        RPushCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushCommandCodec.decode(topology.handle(request), cfg.charset)
}
