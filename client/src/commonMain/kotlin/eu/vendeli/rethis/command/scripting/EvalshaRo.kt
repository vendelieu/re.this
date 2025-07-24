package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.scripting.EvalShaRoCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.evalShaRo(
    sha1: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if(cfg.withSlots) {
        EvalShaRoCommandCodec.encodeWithSlot(charset = cfg.charset, sha1 = sha1, key = key, arg = arg)
    } else {
        EvalShaRoCommandCodec.encode(charset = cfg.charset, sha1 = sha1, key = key, arg = arg)
    }
    return EvalShaRoCommandCodec.decode(topology.handle(request), cfg.charset)
}
