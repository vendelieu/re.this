package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.server.ModuleOption
import eu.vendeli.rethis.codecs.server.ModuleLoadExCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.moduleLoadEx(path: String, vararg options: ModuleOption): Boolean {
    val request = if(cfg.withSlots) {
        ModuleLoadExCommandCodec.encodeWithSlot(charset = cfg.charset, path = path, options = options)
    } else {
        ModuleLoadExCommandCodec.encode(charset = cfg.charset, path = path, options = options)
    }
    return ModuleLoadExCommandCodec.decode(topology.handle(request), cfg.charset)
}
