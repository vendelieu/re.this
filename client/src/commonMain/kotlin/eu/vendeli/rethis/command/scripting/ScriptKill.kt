package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.ScriptKillCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.scriptKill(): Boolean {
    val request = if (cfg.withSlots) {
        ScriptKillCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        ScriptKillCommandCodec.encode(charset = cfg.charset)
    }
    return ScriptKillCommandCodec.decode(topology.handle(request), cfg.charset)
}
