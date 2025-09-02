package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.scripting.FunctionListCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionList(libraryNamePattern: String? = null, withCode: Boolean? = null): List<RType> {
    val request = if(cfg.withSlots) {
        FunctionListCommandCodec.encodeWithSlot(charset = cfg.charset, libraryNamePattern = libraryNamePattern, withCode = withCode)
    } else {
        FunctionListCommandCodec.encode(charset = cfg.charset, libraryNamePattern = libraryNamePattern, withCode = withCode)
    }
    return FunctionListCommandCodec.decode(topology.handle(request), cfg.charset)
}
