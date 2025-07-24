package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.common.MPopResult
import eu.vendeli.rethis.api.spec.common.response.common.MoveDirection
import eu.vendeli.rethis.codecs.list.BlmPopCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.blmPop(
    timeout: Double,
    vararg key: String,
    `where`: MoveDirection,
    count: Long? = null,
): List<MPopResult>? {
    val request = if(cfg.withSlots) {
        BlmPopCommandCodec.encodeWithSlot(charset = cfg.charset, timeout = timeout, key = key, where = where, count = count)
    } else {
        BlmPopCommandCodec.encode(charset = cfg.charset, timeout = timeout, key = key, where = where, count = count)
    }
    return BlmPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
