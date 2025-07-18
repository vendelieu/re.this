package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclLoadCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.aclLoad(): Boolean {
    val request = if(cfg.withSlots) {
        AclLoadCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        AclLoadCommandCodec.encode(charset = cfg.charset, )
    }
    return AclLoadCommandCodec.decode(topology.handle(request), cfg.charset)
}
