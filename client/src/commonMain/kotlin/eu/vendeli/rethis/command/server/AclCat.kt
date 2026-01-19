package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclCatCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclCat(category: String? = null): List<String> {
    val request = if (cfg.withSlots) {
        AclCatCommandCodec.encodeWithSlot(charset = cfg.charset, category = category)
    } else {
        AclCatCommandCodec.encode(charset = cfg.charset, category = category)
    }
    return AclCatCommandCodec.decode(topology.handle(request), cfg.charset)
}
