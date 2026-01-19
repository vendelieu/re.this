package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientGetNameCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientGetName(): String? {
    val request = if (cfg.withSlots) {
        ClientGetNameCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        ClientGetNameCommandCodec.encode(charset = cfg.charset)
    }
    return ClientGetNameCommandCodec.decode(topology.handle(request), cfg.charset)
}
