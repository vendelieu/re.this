package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclSaveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclSave(): Boolean {
    val request = if (cfg.withSlots) {
        AclSaveCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        AclSaveCommandCodec.encode(charset = cfg.charset)
    }
    return AclSaveCommandCodec.decode(topology.handle(request), cfg.charset)
}
