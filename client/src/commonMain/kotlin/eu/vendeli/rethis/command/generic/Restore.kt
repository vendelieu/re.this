package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.generic.RestoreOption
import eu.vendeli.rethis.codecs.generic.RestoreCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.restore(
    key: String,
    ttl: Long,
    serializedValue: ByteArray,
    vararg options: RestoreOption,
): Boolean {
    val request = if(cfg.withSlots) {
        RestoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, ttl = ttl, serializedValue = serializedValue, options = options)
    } else {
        RestoreCommandCodec.encode(charset = cfg.charset, key = key, ttl = ttl, serializedValue = serializedValue, options = options)
    }
    return RestoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
