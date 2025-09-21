package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.ScriptDebugCommandCodec
import eu.vendeli.rethis.shared.request.scripting.ScriptDebugMode
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.scriptDebug(mode: ScriptDebugMode): Boolean {
    val request = if(cfg.withSlots) {
        ScriptDebugCommandCodec.encodeWithSlot(charset = cfg.charset, mode = mode)
    } else {
        ScriptDebugCommandCodec.encode(charset = cfg.charset, mode = mode)
    }
    return ScriptDebugCommandCodec.decode(topology.handle(request), cfg.charset)
}
