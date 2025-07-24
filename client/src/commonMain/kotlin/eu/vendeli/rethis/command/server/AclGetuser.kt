package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.server.AclGetUserCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclGetUser(username: String): Map<String, RType>? {
    val request = if(cfg.withSlots) {
        AclGetUserCommandCodec.encodeWithSlot(charset = cfg.charset, username = username)
    } else {
        AclGetUserCommandCodec.encode(charset = cfg.charset, username = username)
    }
    return AclGetUserCommandCodec.decode(topology.handle(request), cfg.charset)
}
