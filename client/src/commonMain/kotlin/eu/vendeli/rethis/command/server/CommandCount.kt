package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.CommandCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.commandCount(): Long {
    val request = if (cfg.withSlots) {
        CommandCountCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        CommandCountCommandCodec.encode(charset = cfg.charset)
    }
    return CommandCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
