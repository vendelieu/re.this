package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.BzPopMaxCommandCodec
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.bzPopMax(timeout: Double, vararg key: String): ZPopResult? {
    val request = if (cfg.withSlots) {
        BzPopMaxCommandCodec.encodeWithSlot(charset = cfg.charset, timeout = timeout, key = key)
    } else {
        BzPopMaxCommandCodec.encode(charset = cfg.charset, timeout = timeout, key = key)
    }
    return BzPopMaxCommandCodec.decode(topology.handle(request), cfg.charset)
}
