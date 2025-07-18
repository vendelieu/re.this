package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.connection.ClientKillOptions
import eu.vendeli.rethis.codecs.connection.ClientKillCommandCodec
import eu.vendeli.rethis.codecs.connection.ClientKillStringCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.clientKill(vararg filter: ClientKillOptions): Long {
    val request = if(cfg.withSlots) {
        ClientKillCommandCodec.encodeWithSlot(charset = cfg.charset, filter = filter)
    } else {
        ClientKillCommandCodec.encode(charset = cfg.charset, filter = filter)
    }
    return ClientKillCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.clientKillString(ipPort: String): Boolean {
    val request = if(cfg.withSlots) {
        ClientKillStringCommandCodec.encodeWithSlot(charset = cfg.charset, ipPort = ipPort)
    } else {
        ClientKillStringCommandCodec.encode(charset = cfg.charset, ipPort = ipPort)
    }
    return ClientKillStringCommandCodec.decode(topology.handle(request), cfg.charset)
}
