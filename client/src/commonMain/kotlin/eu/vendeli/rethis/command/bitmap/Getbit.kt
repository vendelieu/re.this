package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.bitmap.GetBitCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.getBit(key: String, offset: Long): Long {
    val request = if(cfg.withSlots) {
        GetBitCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, offset = offset)
    } else {
        GetBitCommandCodec.encode(charset = cfg.charset, key = key, offset = offset)
    }
    return GetBitCommandCodec.decode(topology.handle(request), cfg.charset)
}
