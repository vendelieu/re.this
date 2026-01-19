package eu.vendeli.rethis.command.scripting

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.scripting.ScriptExistsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.scriptExists(vararg sha1: String): List<Boolean> {
    val request = if (cfg.withSlots) {
        ScriptExistsCommandCodec.encodeWithSlot(charset = cfg.charset, sha1 = sha1)
    } else {
        ScriptExistsCommandCodec.encode(charset = cfg.charset, sha1 = sha1)
    }
    return ScriptExistsCommandCodec.decode(topology.handle(request), cfg.charset)
}
