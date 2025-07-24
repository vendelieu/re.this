package eu.vendeli.rethis.command.stream

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.stream.Exactement
import eu.vendeli.rethis.api.spec.common.request.stream.TrimmingStrategy
import eu.vendeli.rethis.codecs.stream.XTrimCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.xTrim(
    key: String,
    threshold: String,
    strategy: TrimmingStrategy,
    `operator`: Exactement? = null,
    count: Long? = null,
): Long {
    val request = if(cfg.withSlots) {
        XTrimCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, threshold = threshold, strategy = strategy, operator = operator, count = count)
    } else {
        XTrimCommandCodec.encode(charset = cfg.charset, key = key, threshold = threshold, strategy = strategy, operator = operator, count = count)
    }
    return XTrimCommandCodec.decode(topology.handle(request), cfg.charset)
}
