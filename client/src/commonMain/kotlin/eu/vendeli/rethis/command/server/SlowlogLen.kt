package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.SlowLogLenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.slowLogLen(): Long {
    val request = if(cfg.withSlots) {
        SlowLogLenCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SlowLogLenCommandCodec.encode(charset = cfg.charset, )
    }
    return SlowLogLenCommandCodec.decode(topology.handle(request), cfg.charset)
}
