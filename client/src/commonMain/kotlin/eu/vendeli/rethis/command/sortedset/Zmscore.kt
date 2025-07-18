package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.sortedset.ZMScoreCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.zMScore(key: String, vararg member: String): List<RType>? {
    val request = if(cfg.withSlots) {
        ZMScoreCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        ZMScoreCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return ZMScoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
