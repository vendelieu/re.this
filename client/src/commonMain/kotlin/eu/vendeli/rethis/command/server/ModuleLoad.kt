package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.ModuleLoadCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.moduleLoad(path: String, vararg arg: String): Boolean {
    val request = if(cfg.withSlots) {
        ModuleLoadCommandCodec.encodeWithSlot(charset = cfg.charset, path = path, arg = arg)
    } else {
        ModuleLoadCommandCodec.encode(charset = cfg.charset, path = path, arg = arg)
    }
    return ModuleLoadCommandCodec.decode(topology.handle(request), cfg.charset)
}
