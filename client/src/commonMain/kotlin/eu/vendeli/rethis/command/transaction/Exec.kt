package eu.vendeli.rethis.command.transaction

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.exec(): List<RType>? {
    val request = if(cfg.withSlots) {
        ExecCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ExecCommandCodec.encode(charset = cfg.charset, )
    }
    return ExecCommandCodec.decode(topology.handle(request), cfg.charset)
}
