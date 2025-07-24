package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.list.LInsertPlace
import eu.vendeli.rethis.codecs.list.LInsertCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lInsert(
    key: String,
    `where`: LInsertPlace,
    pivot: String,
    element: String,
): Long {
    val request = if(cfg.withSlots) {
        LInsertCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, where = where, pivot = pivot, element = element)
    } else {
        LInsertCommandCodec.encode(charset = cfg.charset, key = key, where = where, pivot = pivot, element = element)
    }
    return LInsertCommandCodec.decode(topology.handle(request), cfg.charset)
}
