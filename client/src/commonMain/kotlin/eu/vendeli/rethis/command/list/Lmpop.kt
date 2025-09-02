package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.codecs.list.LmPopCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lmPop(
    `where`: MoveDirection,
    vararg key: String,
    count: Long? = null,
): List<MPopResult>? {
    val request = if(cfg.withSlots) {
        LmPopCommandCodec.encodeWithSlot(charset = cfg.charset, where = where, key = key, count = count)
    } else {
        LmPopCommandCodec.encode(charset = cfg.charset, where = where, key = key, count = count)
    }
    return LmPopCommandCodec.decode(topology.handle(request), cfg.charset)
}
