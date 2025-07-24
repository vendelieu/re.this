package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.scripting.FcallCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.fcall(
    function: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if(cfg.withSlots) {
        FcallCommandCodec.encodeWithSlot(charset = cfg.charset, function = function, key = key, arg = arg)
    } else {
        FcallCommandCodec.encode(charset = cfg.charset, function = function, key = key, arg = arg)
    }
    return FcallCommandCodec.decode(topology.handle(request), cfg.charset)
}
