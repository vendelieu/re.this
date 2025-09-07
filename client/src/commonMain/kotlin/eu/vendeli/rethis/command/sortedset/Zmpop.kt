package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZMPopCommandCodec
import eu.vendeli.rethis.shared.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zMPop(
    `where`: ZPopCommonOption,
    vararg key: String,
    count: Long? = null,
): List<MPopResult>? {
    val request = if(cfg.withSlots) {
        ZMPopCommandCodec.encodeWithSlot(charset = cfg.charset, where = where, key = key, count = count)
    } else {
        ZMPopCommandCodec.encode(charset = cfg.charset, where = where, key = key, count = count)
    }
    return ZMPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
