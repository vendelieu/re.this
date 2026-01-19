package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.CommandGetKeysAndFlagsCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.commandGetKeysAndFlags(command: String, vararg arg: String): List<RType> {
    val request = if (cfg.withSlots) {
        CommandGetKeysAndFlagsCommandCodec.encodeWithSlot(charset = cfg.charset, command = command, arg = arg)
    } else {
        CommandGetKeysAndFlagsCommandCodec.encode(charset = cfg.charset, command = command, arg = arg)
    }
    return CommandGetKeysAndFlagsCommandCodec.decode(topology.handle(request), cfg.charset)
}
