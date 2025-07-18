package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.MGetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.mGet(vararg key: String): List<String?> {
    val request = if(cfg.withSlots) {
        MGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        MGetCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return MGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
