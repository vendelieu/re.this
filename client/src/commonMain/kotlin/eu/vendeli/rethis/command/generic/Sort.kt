package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.SortCommandCodec
import eu.vendeli.rethis.codecs.generic.SortStoreCommandCodec
import eu.vendeli.rethis.shared.request.generic.SortOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sort(key: String, vararg option: SortOption): List<String> {
    val request = if(cfg.withSlots) {
        SortCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, option = option)
    } else {
        SortCommandCodec.encode(charset = cfg.charset, key = key, option = option)
    }
    return SortCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.sortStore(
    key: String,
    storeDestination: String,
    vararg option: SortOption,
): Long {
    val request = if(cfg.withSlots) {
        SortStoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, storeDestination = storeDestination, option = option)
    } else {
        SortStoreCommandCodec.encode(charset = cfg.charset, key = key, storeDestination = storeDestination, option = option)
    }
    return SortStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
