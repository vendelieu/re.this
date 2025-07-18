package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.CommandGetKeysCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.commandGetKeys(command: String, vararg arg: String): List<String> {
    val request = if(cfg.withSlots) {
        CommandGetKeysCommandCodec.encodeWithSlot(charset = cfg.charset, command = command, arg = arg)
    } else {
        CommandGetKeysCommandCodec.encode(charset = cfg.charset, command = command, arg = arg)
    }
    return CommandGetKeysCommandCodec.decode(topology.handle(request), cfg.charset)
}
