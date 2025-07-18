package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.server.CommandDocsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Map

public suspend fun ReThis.commandDocs(vararg commandName: String): Map<String, RType> {
    val request = if(cfg.withSlots) {
        CommandDocsCommandCodec.encodeWithSlot(charset = cfg.charset, commandName = commandName)
    } else {
        CommandDocsCommandCodec.encode(charset = cfg.charset, commandName = commandName)
    }
    return CommandDocsCommandCodec.decode(topology.handle(request), cfg.charset)
}
