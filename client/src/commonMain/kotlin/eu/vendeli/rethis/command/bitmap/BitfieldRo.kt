package eu.vendeli.rethis.command.bitmap

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.codecs.bitmap.BitfieldRoCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.bitfieldRo(key: String, vararg options: BitfieldOption.Get): List<Long> {
    val request = if(cfg.withSlots) {
        BitfieldRoCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, options = options)
    } else {
        BitfieldRoCommandCodec.encode(charset = cfg.charset, key = key, options = options)
    }
    return BitfieldRoCommandCodec.decode(topology.handle(request), cfg.charset)
}
