package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.RPushBSCommandCodec
import eu.vendeli.rethis.codecs.list.RPushCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.rPushBS(key: String, vararg element: ByteString): Long {
    val request = if(cfg.withSlots) {
        RPushBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushBSCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.rPush(key: String, vararg element: String): Long {
    val request = if(cfg.withSlots) {
        RPushCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element)
    } else {
        RPushCommandCodec.encode(charset = cfg.charset, key = key, element = element)
    }
    return RPushCommandCodec.decode(topology.handle(request), cfg.charset)
}
