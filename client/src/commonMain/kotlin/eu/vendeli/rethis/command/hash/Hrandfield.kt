package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HRandFieldBACommandCodec
import eu.vendeli.rethis.codecs.hash.HRandFieldCommandCodec
import eu.vendeli.rethis.codecs.hash.HRandFieldCountCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hRandField(key: String): String? {
    val request = if(cfg.withSlots) {
        HRandFieldCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HRandFieldCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HRandFieldCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.hRandFieldBA(key: String): ByteArray? {
    val request = if(cfg.withSlots) {
        HRandFieldBACommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HRandFieldBACommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HRandFieldBACommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.hRandFieldCount(
    key: String,
    count: Long,
    withValues: Boolean? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        HRandFieldCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count, withValues = withValues)
    } else {
        HRandFieldCountCommandCodec.encode(charset = cfg.charset, key = key, count = count, withValues = withValues)
    }
    return HRandFieldCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
