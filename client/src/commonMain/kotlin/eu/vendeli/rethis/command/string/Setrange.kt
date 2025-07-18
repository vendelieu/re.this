package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.SetRangeCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.setRange(
    key: String,
    offset: Long,
    `value`: String,
): Long {
    val request = if(cfg.withSlots) {
        SetRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, offset = offset, value = value)
    } else {
        SetRangeCommandCodec.encode(charset = cfg.charset, key = key, offset = offset, value = value)
    }
    return SetRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
