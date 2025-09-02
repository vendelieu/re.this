package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.RandomKeyCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.randomKey(): String? {
    val request = if(cfg.withSlots) {
        RandomKeyCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        RandomKeyCommandCodec.encode(charset = cfg.charset, )
    }
    return RandomKeyCommandCodec.decode(topology.handle(request), cfg.charset)
}
