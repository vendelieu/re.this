package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.FunctionDeleteCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionDelete(libraryName: String): Boolean {
    val request = if(cfg.withSlots) {
        FunctionDeleteCommandCodec.encodeWithSlot(charset = cfg.charset, libraryName = libraryName)
    } else {
        FunctionDeleteCommandCodec.encode(charset = cfg.charset, libraryName = libraryName)
    }
    return FunctionDeleteCommandCodec.decode(topology.handle(request), cfg.charset)
}
