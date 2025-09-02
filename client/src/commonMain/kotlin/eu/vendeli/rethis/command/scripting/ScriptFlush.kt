package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.codecs.scripting.ScriptFlushCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.scriptFlush(flushType: FlushType? = null): Boolean {
    val request = if(cfg.withSlots) {
        ScriptFlushCommandCodec.encodeWithSlot(charset = cfg.charset, flushType = flushType)
    } else {
        ScriptFlushCommandCodec.encode(charset = cfg.charset, flushType = flushType)
    }
    return ScriptFlushCommandCodec.decode(topology.handle(request), cfg.charset)
}
