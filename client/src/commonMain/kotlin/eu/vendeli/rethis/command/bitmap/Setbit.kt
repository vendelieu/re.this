package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.bitmap.SetBitCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.setBit(
    key: String,
    offset: Long,
    `value`: Long,
): Long {
    val request = if (cfg.withSlots) {
        SetBitCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, offset = offset, value = value)
    } else {
        SetBitCommandCodec.encode(charset = cfg.charset, key = key, offset = offset, value = value)
    }
    return SetBitCommandCodec.decode(topology.handle(request), cfg.charset)
}
