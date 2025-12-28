package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HGetBSCommandCodec
import eu.vendeli.rethis.codecs.hash.HGetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.hGetBS(key: String, `field`: String): ByteString? {
    val request = if(cfg.withSlots) {
        HGetBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HGetBSCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HGetBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.hGet(key: String, `field`: String): String? {
    val request = if(cfg.withSlots) {
        HGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HGetCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
