package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ReadOnlyCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.readOnly(): Boolean {
    val request = if (cfg.withSlots) {
        ReadOnlyCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        ReadOnlyCommandCodec.encode(charset = cfg.charset)
    }
    return ReadOnlyCommandCodec.decode(topology.handle(request), cfg.charset)
}
