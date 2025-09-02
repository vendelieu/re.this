package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.ScriptLoadCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.scriptLoad(script: String): String {
    val request = if(cfg.withSlots) {
        ScriptLoadCommandCodec.encodeWithSlot(charset = cfg.charset, script = script)
    } else {
        ScriptLoadCommandCodec.encode(charset = cfg.charset, script = script)
    }
    return ScriptLoadCommandCodec.decode(topology.handle(request), cfg.charset)
}
