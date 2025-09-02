package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclListCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclList(): List<String> {
    val request = if(cfg.withSlots) {
        AclListCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        AclListCommandCodec.encode(charset = cfg.charset, )
    }
    return AclListCommandCodec.decode(topology.handle(request), cfg.charset)
}
