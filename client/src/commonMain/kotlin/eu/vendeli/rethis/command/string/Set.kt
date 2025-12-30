package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.SetBACommandCodec
import eu.vendeli.rethis.codecs.string.SetCommandCodec
import eu.vendeli.rethis.shared.request.string.SetOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.`set`(
    key: String,
    `value`: String,
    vararg options: SetOption,
): String? {
    val request = if(cfg.withSlots) {
        SetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, value = value, options = options)
    } else {
        SetCommandCodec.encode(charset = cfg.charset, key = key, value = value, options = options)
    }
    return SetCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.setBA(
    key: String,
    `value`: ByteArray,
    vararg options: SetOption,
): ByteArray? {
    val request = if(cfg.withSlots) {
        SetBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key, value = value, options = options)
    } else {
        SetBACommandCodec.encode(charset = cfg.charset, key = key, value = value, options = options)
    }
    return SetBACommandCodec.decode(topology.handle(request), cfg.charset)
}
