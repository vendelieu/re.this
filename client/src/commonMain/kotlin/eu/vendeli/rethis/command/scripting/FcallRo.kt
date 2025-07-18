package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.scripting.FcallRoCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.fcallRo(
    function: String,
    vararg key: String,
    arg: List<String>,
): RType {
    val request = if(cfg.withSlots) {
        FcallRoCommandCodec.encodeWithSlot(charset = cfg.charset, function = function, key = key, arg = arg)
    } else {
        FcallRoCommandCodec.encode(charset = cfg.charset, function = function, key = key, arg = arg)
    }
    return FcallRoCommandCodec.decode(topology.handle(request), cfg.charset)
}
