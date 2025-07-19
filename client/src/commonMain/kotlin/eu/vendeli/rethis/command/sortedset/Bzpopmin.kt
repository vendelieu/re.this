package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.stream.ZPopResult
import eu.vendeli.rethis.codecs.sortedset.BzPopMinCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Double
import kotlin.String

public suspend fun ReThis.bzPopMin(timeout: Double, vararg key: String): ZPopResult? {
    val request = if(cfg.withSlots) {
        BzPopMinCommandCodec.encodeWithSlot(charset = cfg.charset, timeout = timeout, key = key)
    } else {
        BzPopMinCommandCodec.encode(charset = cfg.charset, timeout = timeout, key = key)
    }
    return BzPopMinCommandCodec.decode(topology.handle(request), cfg.charset)
}
