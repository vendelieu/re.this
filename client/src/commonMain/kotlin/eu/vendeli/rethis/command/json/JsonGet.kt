package eu.vendeli.rethis.command.json

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.json.JsonGetBSCommandCodec
import eu.vendeli.rethis.codecs.json.JsonGetCommandCodec
import eu.vendeli.rethis.shared.request.json.JsonGetOption
import eu.vendeli.rethis.topology.handle
import kotlinx.io.bytestring.ByteString

public suspend fun ReThis.jsonGetBS(key: String, vararg options: JsonGetOption): ByteString? {
    val request = if(cfg.withSlots) {
        JsonGetBSCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, options = options)
    } else {
        JsonGetBSCommandCodec.encode(charset = cfg.charset, key = key, options = options)
    }
    return JsonGetBSCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.jsonGet(key: String, vararg options: JsonGetOption): String? {
    val request = if(cfg.withSlots) {
        JsonGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, options = options)
    } else {
        JsonGetCommandCodec.encode(charset = cfg.charset, key = key, options = options)
    }
    return JsonGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
