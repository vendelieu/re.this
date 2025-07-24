package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.AclGenPassCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.aclGenPass(bits: Long? = null): String {
    val request = if(cfg.withSlots) {
        AclGenPassCommandCodec.encodeWithSlot(charset = cfg.charset, bits = bits)
    } else {
        AclGenPassCommandCodec.encode(charset = cfg.charset, bits = bits)
    }
    return AclGenPassCommandCodec.decode(topology.handle(request), cfg.charset)
}
