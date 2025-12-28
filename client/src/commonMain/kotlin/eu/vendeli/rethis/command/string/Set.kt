package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.SetBSCommandCodec
import eu.vendeli.rethis.codecs.string.SetCommandCodec
import eu.vendeli.rethis.shared.request.string.SetOption
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

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

public suspend fun ReThis.setBS(
    key: String,
    `value`: ByteString,
    vararg options: SetOption,
): ByteString? {
    val request = if(cfg.withSlots) {
        SetBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, value = value, options = options)
    } else {
        SetBSCommandCodec.encode(charset = cfg.charset, key = key, value = value, options = options)
    }
    return SetBSCommandCodec.decode(topology.handle(request), cfg.charset)
}
