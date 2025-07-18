package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.scripting.FunctionStatsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Map

public suspend fun ReThis.functionStats(): Map<String, RType> {
    val request = if(cfg.withSlots) {
        FunctionStatsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        FunctionStatsCommandCodec.encode(charset = cfg.charset, )
    }
    return FunctionStatsCommandCodec.decode(topology.handle(request), cfg.charset)
}
