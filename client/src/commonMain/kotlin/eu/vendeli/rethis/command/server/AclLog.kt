package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclLogCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclLog(count: Long? = null, reset: Boolean? = null): List<String> {
    val request = if(cfg.withSlots) {
        AclLogCommandCodec.encodeWithSlot(charset = cfg.charset, count = count, reset = reset)
    } else {
        AclLogCommandCodec.encode(charset = cfg.charset, count = count, reset = reset)
    }
    return AclLogCommandCodec.decode(topology.handle(request), cfg.charset)
}
