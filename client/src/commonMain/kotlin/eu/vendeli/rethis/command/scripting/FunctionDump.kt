package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.FunctionDumpCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.functionDump(): ByteArray? {
    val request = if (cfg.withSlots) {
        FunctionDumpCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        FunctionDumpCommandCodec.encode(charset = cfg.charset)
    }
    return FunctionDumpCommandCodec.decode(topology.handle(request), cfg.charset)
}
