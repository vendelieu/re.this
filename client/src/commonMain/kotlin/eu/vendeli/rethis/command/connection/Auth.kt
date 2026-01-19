package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.AuthCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.auth(username: String? = null, password: CharArray): Boolean {
    val request = if (cfg.withSlots) {
        AuthCommandCodec.encodeWithSlot(charset = cfg.charset, username = username, password = password)
    } else {
        AuthCommandCodec.encode(charset = cfg.charset, username = username, password = password)
    }
    return AuthCommandCodec.decode(topology.handle(request), cfg.charset)
}
