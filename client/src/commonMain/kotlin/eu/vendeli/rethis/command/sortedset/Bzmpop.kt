package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.codecs.sortedset.BzMPopCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.bzMPop(
    timeout: Double,
    `where`: ZPopCommonOption,
    vararg key: String,
    count: Long? = null,
): List<MPopResult>? {
    val request = if(cfg.withSlots) {
        BzMPopCommandCodec.encodeWithSlot(charset = cfg.charset, timeout = timeout, where = where, key = key, count = count)
    } else {
        BzMPopCommandCodec.encode(charset = cfg.charset, timeout = timeout, where = where, key = key, count = count)
    }
    return BzMPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
