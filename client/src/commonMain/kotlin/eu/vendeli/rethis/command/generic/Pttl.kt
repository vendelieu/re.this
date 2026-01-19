package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.PTtlCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.pTtl(key: String): Long {
    val request = if (cfg.withSlots) {
        PTtlCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        PTtlCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return PTtlCommandCodec.decode(topology.handle(request), cfg.charset)
}
