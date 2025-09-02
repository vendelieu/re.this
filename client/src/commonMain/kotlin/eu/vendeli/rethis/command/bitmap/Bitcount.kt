package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.bitmap.BitmapUnit
import eu.vendeli.rethis.shared.request.bitmap.Range
import eu.vendeli.rethis.codecs.bitmap.BitCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bitCount(
    key: String,
    range: Range? = null,
    unit: BitmapUnit? = null,
): Long {
    val request = if(cfg.withSlots) {
        BitCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, range = range, unit = unit)
    } else {
        BitCountCommandCodec.encode(charset = cfg.charset, key = key, range = range, unit = unit)
    }
    return BitCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
