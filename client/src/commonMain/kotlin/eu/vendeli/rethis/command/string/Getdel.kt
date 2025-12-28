package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetDelBSCommandCodec
import eu.vendeli.rethis.codecs.string.GetDelCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.getDel(key: String): String? {
    val request = if(cfg.withSlots) {
        GetDelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetDelCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetDelCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.getDelBS(key: String): ByteString? {
    val request = if(cfg.withSlots) {
        GetDelBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetDelBSCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetDelBSCommandCodec.decode(topology.handle(request), cfg.charset)
}
