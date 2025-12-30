package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetBACommandCodec
import eu.vendeli.rethis.codecs.string.GetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.getBA(key: String): ByteArray? {
    val request = if(cfg.withSlots) {
        GetBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetBACommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetBACommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.`get`(key: String): String? {
    val request = if(cfg.withSlots) {
        GetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetCommandCodec.decode(topology.handle(request), cfg.charset)
}
