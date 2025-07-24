package eu.vendeli.rethis.command.connection

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.connection.HelloAuth
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.connection.HelloCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hello(
    protover: Long? = null,
    auth: HelloAuth? = null,
    clientname: String? = null,
): Map<String, RType> {
    val request = if(cfg.withSlots) {
        HelloCommandCodec.encodeWithSlot(charset = cfg.charset, protover = protover, auth = auth, clientname = clientname)
    } else {
        HelloCommandCodec.encode(charset = cfg.charset, protover = protover, auth = auth, clientname = clientname)
    }
    return HelloCommandCodec.decode(topology.handle(request), cfg.charset)
}
