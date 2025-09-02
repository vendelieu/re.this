package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.server.RoleCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.role(): List<RType> {
    val request = if(cfg.withSlots) {
        RoleCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        RoleCommandCodec.encode(charset = cfg.charset, )
    }
    return RoleCommandCodec.decode(topology.handle(request), cfg.charset)
}
