package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclSetUserCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.aclSetUser(username: String, vararg rule: String): Boolean {
    val request = if(cfg.withSlots) {
        AclSetUserCommandCodec.encodeWithSlot(charset = cfg.charset, username = username, rule = rule)
    } else {
        AclSetUserCommandCodec.encode(charset = cfg.charset, username = username, rule = rule)
    }
    return AclSetUserCommandCodec.decode(topology.handle(request), cfg.charset)
}
