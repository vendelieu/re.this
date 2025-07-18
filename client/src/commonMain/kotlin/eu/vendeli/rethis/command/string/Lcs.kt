package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.string.LcsMode
import eu.vendeli.rethis.api.spec.common.request.string.MinMatchLen
import eu.vendeli.rethis.api.spec.common.response.LcsResult
import eu.vendeli.rethis.codecs.string.LcsCommandCodec
import eu.vendeli.rethis.codecs.string.LcsDetailedCommandCodec
import eu.vendeli.rethis.codecs.string.LcsLengthCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.lcs(key1: String, key2: String): String {
    val request = if(cfg.withSlots) {
        LcsCommandCodec.encodeWithSlot(charset = cfg.charset, key1 = key1, key2 = key2)
    } else {
        LcsCommandCodec.encode(charset = cfg.charset, key1 = key1, key2 = key2)
    }
    return LcsCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.lcsDetailed(
    key1: String,
    key2: String,
    mode: LcsMode.IDX,
    len: MinMatchLen? = null,
    withMatchLen: Boolean? = null,
): LcsResult {
    val request = if(cfg.withSlots) {
        LcsDetailedCommandCodec.encodeWithSlot(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode, len = len, withMatchLen = withMatchLen)
    } else {
        LcsDetailedCommandCodec.encode(charset = cfg.charset, key1 = key1, key2 = key2, mode = mode, len = len, withMatchLen = withMatchLen)
    }
    return LcsDetailedCommandCodec.decode(topology.handle(request), cfg.charset)
}

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
