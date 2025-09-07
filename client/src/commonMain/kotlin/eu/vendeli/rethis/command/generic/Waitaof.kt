package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.WaitAofCommandCodec
import eu.vendeli.rethis.shared.response.common.WaitAofResult
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.waitAof(
    numlocal: Long,
    numreplicas: Long,
    timeout: Long,
): WaitAofResult {
    val request = if(cfg.withSlots) {
        WaitAofCommandCodec.encodeWithSlot(charset = cfg.charset, numlocal = numlocal, numreplicas = numreplicas, timeout = timeout)
    } else {
        WaitAofCommandCodec.encode(charset = cfg.charset, numlocal = numlocal, numreplicas = numreplicas, timeout = timeout)
    }
    return WaitAofCommandCodec.decode(topology.handle(request), cfg.charset)
}
