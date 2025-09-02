package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.bitmap.BitmapUnit
import eu.vendeli.rethis.codecs.bitmap.BitPosCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bitPos(
    key: String,
    bit: Long,
    start: Long? = null,
    end: Long? = null,
    unit: BitmapUnit? = null,
): Long {
    val request = if(cfg.withSlots) {
        BitPosCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, bit = bit, start = start, end = end, unit = unit)
    } else {
        BitPosCommandCodec.encode(charset = cfg.charset, key = key, bit = bit, start = start, end = end, unit = unit)
    }
    return BitPosCommandCodec.decode(topology.handle(request), cfg.charset)
}
