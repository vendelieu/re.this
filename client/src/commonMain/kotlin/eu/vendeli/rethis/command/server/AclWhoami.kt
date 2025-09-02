package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclWhoAmICommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclWhoAmI(): String {
    val request = if(cfg.withSlots) {
        AclWhoAmICommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        AclWhoAmICommandCodec.encode(charset = cfg.charset, )
    }
    return AclWhoAmICommandCodec.decode(topology.handle(request), cfg.charset)
}
