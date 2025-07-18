package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.scripting.EvalShaCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.evalSha(
    sha1: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if(cfg.withSlots) {
        EvalShaCommandCodec.encodeWithSlot(charset = cfg.charset, sha1 = sha1, key = key, arg = arg)
    } else {
        EvalShaCommandCodec.encode(charset = cfg.charset, sha1 = sha1, key = key, arg = arg)
    }
    return EvalShaCommandCodec.decode(topology.handle(request), cfg.charset)
}
