package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.DumpCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.dump(key: String): ByteArray? {
    val request = if(cfg.withSlots) {
        DumpCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        DumpCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return DumpCommandCodec.decode(topology.handle(request), cfg.charset)
}
