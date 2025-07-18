package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.generic.CopyOption
import eu.vendeli.rethis.codecs.generic.CopyCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.copy(
    source: String,
    destination: String,
    vararg option: CopyOption,
): Boolean {
    val request = if(cfg.withSlots) {
        CopyCommandCodec.encodeWithSlot(charset = cfg.charset, source = source, destination = destination, option = option)
    } else {
        CopyCommandCodec.encode(charset = cfg.charset, source = source, destination = destination, option = option)
    }
    return CopyCommandCodec.decode(topology.handle(request), cfg.charset)
}
