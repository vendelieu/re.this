package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZInterCommandCodec
import eu.vendeli.rethis.shared.request.sortedset.ZAggregate
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zInter(
    vararg key: String,
    weight: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean? = null,
): List<String> {
    val request = if (cfg.withSlots) {
        ZInterCommandCodec.encodeWithSlot(
            charset = cfg.charset,
            key = key,
            weight = weight,
            aggregate = aggregate,
            withScores = withScores,
        )
    } else {
        ZInterCommandCodec.encode(
            charset = cfg.charset,
            key = key,
            weight = weight,
            aggregate = aggregate,
            withScores = withScores,
        )
    }
    return ZInterCommandCodec.decode(topology.handle(request), cfg.charset)
}
