package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.string.LcsMode
import eu.vendeli.rethis.shared.request.string.MinMatchLen
import eu.vendeli.rethis.shared.response.string.LcsResult
import eu.vendeli.rethis.codecs.string.LcsCommandCodec
import eu.vendeli.rethis.codecs.string.LcsDetailedCommandCodec
import eu.vendeli.rethis.codecs.string.LcsLengthCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lcsLength(
    key1: String,
    key2: String,
    mode: LcsMode.LEN,
): Long {
    val request = if(cfg.withSlots) {
        LcsLengthCommandCodec.encodeWithSlot(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode)
    } else {
        LcsLengthCommandCodec.encode(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode)
    }
    return LcsLengthCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lcsDetailed(
    key1: String,
    key2: String,
    mode: LcsMode.IDX,
    minMatchLen: MinMatchLen? = null,
    withMatchLen: Boolean? = null,
): LcsResult {
    val request = if(cfg.withSlots) {
        LcsDetailedCommandCodec.encodeWithSlot(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode, minMatchLen = minMatchLen, withMatchLen = withMatchLen)
    } else {
        LcsDetailedCommandCodec.encode(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode, minMatchLen = minMatchLen, withMatchLen = withMatchLen)
    }
    return LcsDetailedCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lcs(key1: String, key2: String): String {
    val request = if(cfg.withSlots) {
        LcsCommandCodec.encodeWithSlot(charset = cfg.charset, key1 = key1, key2 = key2)
    } else {
        LcsCommandCodec.encode(charset = cfg.charset, key1 = key1, key2 = key2)
    }
    return LcsCommandCodec.decode(topology.handle(request), cfg.charset)
}
