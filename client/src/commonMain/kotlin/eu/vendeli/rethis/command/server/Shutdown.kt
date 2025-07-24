package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.server.SaveSelector
import eu.vendeli.rethis.api.spec.common.request.server.ShutdownOptions
import eu.vendeli.rethis.codecs.server.ShutdownCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.shutdown(saveSelector: SaveSelector? = null, vararg options: ShutdownOptions): Boolean {
    val request = if(cfg.withSlots) {
        ShutdownCommandCodec.encodeWithSlot(charset = cfg.charset, saveSelector = saveSelector, options = options)
    } else {
        ShutdownCommandCodec.encode(charset = cfg.charset, saveSelector = saveSelector, options = options)
    }
    return ShutdownCommandCodec.decode(topology.handle(request), cfg.charset)
}
