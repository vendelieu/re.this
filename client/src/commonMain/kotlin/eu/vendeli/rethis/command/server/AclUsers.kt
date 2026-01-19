package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclUsersCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclUsers(): List<String> {
    val request = if (cfg.withSlots) {
        AclUsersCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        AclUsersCommandCodec.encode(charset = cfg.charset)
    }
    return AclUsersCommandCodec.decode(topology.handle(request), cfg.charset)
}
