package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.ModuleUnloadCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.moduleUnload(name: String): Boolean {
    val request = if(cfg.withSlots) {
        ModuleUnloadCommandCodec.encodeWithSlot(charset = cfg.charset, name = name)
    } else {
        ModuleUnloadCommandCodec.encode(charset = cfg.charset, name = name)
    }
    return ModuleUnloadCommandCodec.decode(topology.handle(request), cfg.charset)
}
