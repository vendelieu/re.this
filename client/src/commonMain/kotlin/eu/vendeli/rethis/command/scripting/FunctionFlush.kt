package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.codecs.scripting.FunctionFlushCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionFlush(flushType: FlushType): Boolean {
    val request = if(cfg.withSlots) {
        FunctionFlushCommandCodec.encodeWithSlot(charset = cfg.charset, flushType = flushType)
    } else {
        FunctionFlushCommandCodec.encode(charset = cfg.charset, flushType = flushType)
    }
    return FunctionFlushCommandCodec.decode(topology.handle(request), cfg.charset)
}
