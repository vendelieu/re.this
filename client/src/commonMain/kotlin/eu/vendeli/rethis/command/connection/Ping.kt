package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.ping(message: String? = null): String {
    val request = if (cfg.withSlots) {
        PingCommandCodec.encodeWithSlot(charset = cfg.charset, message = message)
    } else {
        PingCommandCodec.encode(charset = cfg.charset, message = message)
    }
    return PingCommandCodec.decode(topology.handle(request), cfg.charset)
}
