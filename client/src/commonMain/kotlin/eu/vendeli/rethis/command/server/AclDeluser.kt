package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclDelUserCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.aclDelUser(vararg username: String): Long {
    val request = if(cfg.withSlots) {
        AclDelUserCommandCodec.encodeWithSlot(charset = cfg.charset, username = username)
    } else {
        AclDelUserCommandCodec.encode(charset = cfg.charset, username = username)
    }
    return AclDelUserCommandCodec.decode(topology.handle(request), cfg.charset)
}
