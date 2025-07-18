package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.FunctionLoadCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.functionLoad(functionCode: String, replace: Boolean? = null): String {
    val request = if(cfg.withSlots) {
        FunctionLoadCommandCodec.encodeWithSlot(charset = cfg.charset, functionCode = functionCode, replace = replace)
    } else {
        FunctionLoadCommandCodec.encode(charset = cfg.charset, functionCode = functionCode, replace = replace)
    }
    return FunctionLoadCommandCodec.decode(topology.handle(request), cfg.charset)
}
