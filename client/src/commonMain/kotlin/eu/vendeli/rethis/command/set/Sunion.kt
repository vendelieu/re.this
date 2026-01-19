package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SUnionCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sUnion(vararg key: String): Set<String> {
    val request = if (cfg.withSlots) {
        SUnionCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SUnionCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SUnionCommandCodec.decode(topology.handle(request), cfg.charset)
}
