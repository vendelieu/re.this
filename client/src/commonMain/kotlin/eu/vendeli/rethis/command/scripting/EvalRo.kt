package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.EvalRoCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.evalRo(
    script: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if(cfg.withSlots) {
        EvalRoCommandCodec.encodeWithSlot(charset = cfg.charset, script = script, key = key, arg = arg)
    } else {
        EvalRoCommandCodec.encode(charset = cfg.charset, script = script, key = key, arg = arg)
    }
    return EvalRoCommandCodec.decode(topology.handle(request), cfg.charset)
}
