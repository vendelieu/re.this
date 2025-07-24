package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.FunctionKillCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionKill(): Boolean {
    val request = if(cfg.withSlots) {
        FunctionKillCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        FunctionKillCommandCodec.encode(charset = cfg.charset, )
    }
    return FunctionKillCommandCodec.decode(topology.handle(request), cfg.charset)
}
