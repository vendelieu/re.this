package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.QuitCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean

public suspend fun ReThis.quit(): Boolean {
    val request = if(cfg.withSlots) {
        QuitCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        QuitCommandCodec.encode(charset = cfg.charset, )
    }
    return QuitCommandCodec.decode(topology.handle(request), cfg.charset)
}
