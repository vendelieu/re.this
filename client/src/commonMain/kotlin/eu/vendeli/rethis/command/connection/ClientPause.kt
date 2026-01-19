package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.ClientPauseCommandCodec
import eu.vendeli.rethis.shared.request.connection.ClientPauseMode
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clientPause(timeout: Long, mode: ClientPauseMode? = null): Boolean {
    val request = if (cfg.withSlots) {
        ClientPauseCommandCodec.encodeWithSlot(charset = cfg.charset, timeout = timeout, mode = mode)
    } else {
        ClientPauseCommandCodec.encode(charset = cfg.charset, timeout = timeout, mode = mode)
    }
    return ClientPauseCommandCodec.decode(topology.handle(request), cfg.charset)
}
