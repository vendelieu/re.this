package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.stream.XPendingMainFilter
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.stream.XPendingCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.xPending(
    key: String,
    group: String,
    filters: XPendingMainFilter? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        XPendingCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, group = group, filters = filters)
    } else {
        XPendingCommandCodec.encode(charset = cfg.charset, key = key, group = group, filters = filters)
    }
    return XPendingCommandCodec.decode(topology.handle(request), cfg.charset)
}
