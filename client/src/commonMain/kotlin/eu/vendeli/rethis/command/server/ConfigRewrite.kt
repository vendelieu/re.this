package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.ConfigRewriteCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.configRewrite(): Boolean {
    val request = if(cfg.withSlots) {
        ConfigRewriteCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ConfigRewriteCommandCodec.encode(charset = cfg.charset, )
    }
    return ConfigRewriteCommandCodec.decode(topology.handle(request), cfg.charset)
}
