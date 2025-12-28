package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.GetBSCommandCodec
import eu.vendeli.rethis.codecs.string.GetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.getBS(key: String): ByteString? {
    val request = if(cfg.withSlots) {
        GetBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetBSCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.`get`(key: String): String? {
    val request = if(cfg.withSlots) {
        GetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        GetCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return GetCommandCodec.decode(topology.handle(request), cfg.charset)
}
