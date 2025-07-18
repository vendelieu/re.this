package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ReadWriteCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.readWrite(): Boolean {
    val request = if(cfg.withSlots) {
        ReadWriteCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ReadWriteCommandCodec.encode(charset = cfg.charset, )
    }
    return ReadWriteCommandCodec.decode(topology.handle(request), cfg.charset)
}
