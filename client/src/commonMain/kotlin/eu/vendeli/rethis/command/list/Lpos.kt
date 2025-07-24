package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption
import eu.vendeli.rethis.codecs.list.LPosCommandCodec
import eu.vendeli.rethis.codecs.list.LPosCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lPosCount(
    key: String,
    element: String,
    numMatches: Long,
    vararg option: LPosOption,
): List<Long> {
    val request = if(cfg.withSlots) {
        LPosCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element, numMatches = numMatches, option = option)
    } else {
        LPosCountCommandCodec.encode(charset = cfg.charset, key = key, element = element, numMatches = numMatches, option = option)
    }
    return LPosCountCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lPos(
    key: String,
    element: String,
    vararg option: LPosOption,
): Long? {
    val request = if(cfg.withSlots) {
        LPosCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, element = element, option = option)
    } else {
        LPosCommandCodec.encode(charset = cfg.charset, key = key, element = element, option = option)
    }
    return LPosCommandCodec.decode(topology.handle(request), cfg.charset)
}
