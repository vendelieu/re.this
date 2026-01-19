package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.EvalCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.eval(
    script: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if (cfg.withSlots) {
        EvalCommandCodec.encodeWithSlot(charset = cfg.charset, script = script, key = key, arg = arg)
    } else {
        EvalCommandCodec.encode(charset = cfg.charset, script = script, key = key, arg = arg)
    }
    return EvalCommandCodec.decode(topology.handle(request), cfg.charset)
}
