package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SPopBSCommandCodec
import eu.vendeli.rethis.codecs.set.SPopCommandCodec
import eu.vendeli.rethis.codecs.set.SPopCountCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.sPop(key: String): String? {
    val request = if(cfg.withSlots) {
        SPopCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SPopCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SPopCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.sPopBS(key: String): ByteString? {
    val request = if(cfg.withSlots) {
        SPopBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SPopBSCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SPopBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.sPopCount(key: String, count: Long? = null): List<String> {
    val request = if(cfg.withSlots) {
        SPopCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count)
    } else {
        SPopCountCommandCodec.encode(charset = cfg.charset, key = key, count = count)
    }
    return SPopCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
