package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.codecs.bitmap.BitfieldCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.bitfield(key: String, vararg operation: BitfieldOption): List<Long>? {
    val request = if(cfg.withSlots) {
        BitfieldCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, operation = operation)
    } else {
        BitfieldCommandCodec.encode(charset = cfg.charset, key = key, operation = operation)
    }
    return BitfieldCommandCodec.decode(topology.handle(request), cfg.charset)
}
