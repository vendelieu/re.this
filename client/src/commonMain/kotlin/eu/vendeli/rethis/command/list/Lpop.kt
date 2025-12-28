package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LPopBSCommandCodec
import eu.vendeli.rethis.codecs.list.LPopCommandCodec
import eu.vendeli.rethis.codecs.list.LPopCountCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.lPopBS(key: String): ByteString? {
    val request = if(cfg.withSlots) {
        LPopBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        LPopBSCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return LPopBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lPop(key: String): String? {
    val request = if(cfg.withSlots) {
        LPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        LPopCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return LPopCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lPopCount(key: String, count: Long? = null): List<String> {
    val request = if(cfg.withSlots) {
        LPopCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        LPopCountCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return LPopCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
