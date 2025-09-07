package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.ModuleListCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.moduleList(): List<RType> {
    val request = if(cfg.withSlots) {
        ModuleListCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ModuleListCommandCodec.encode(charset = cfg.charset, )
    }
    return ModuleListCommandCodec.decode(topology.handle(request), cfg.charset)
}
