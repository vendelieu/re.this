package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption
import eu.vendeli.rethis.codecs.generic.SortRoCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.sortRo(key: String, vararg option: SortOption): List<String> {
    val request = if(cfg.withSlots) {
        SortRoCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, option = option)
    } else {
        SortRoCommandCodec.encode(charset = cfg.charset, key = key, option = option)
    }
    return SortRoCommandCodec.decode(topology.handle(request), cfg.charset)
}
