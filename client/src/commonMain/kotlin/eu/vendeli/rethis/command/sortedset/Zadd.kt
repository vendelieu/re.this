package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZAddCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZAddExtendedCommandCodec
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.response.stream.ZMember
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zAdd(
    key: String,
    vararg `data`: ZMember,
    condition: UpdateStrategyOption.ExistenceRule? = null,
    comparison: UpdateStrategyOption.ComparisonRule? = null,
    change: Boolean? = null,
): Long? {
    val request = if(cfg.withSlots) {
        ZAddCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, data = data, condition = condition, comparison = comparison, change = change)
    } else {
        ZAddCommandCodec.encode(charset = cfg.charset, key = key, data = data, condition = condition, comparison = comparison, change = change)
    }
    return ZAddCommandCodec.decode(topology.handle(request), cfg.charset)
}

public suspend fun ReThis.zAddExtended(
    key: String,
    vararg `data`: ZMember,
    condition: UpdateStrategyOption.ExistenceRule? = null,
    comparison: UpdateStrategyOption.ComparisonRule? = null,
    change: Boolean? = null,
    increment: Boolean? = null,
): Double? {
    val request = if(cfg.withSlots) {
        ZAddExtendedCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, data = data, condition = condition, comparison = comparison, change = change, increment = increment)
    } else {
        ZAddExtendedCommandCodec.encode(charset = cfg.charset, key = key, data = data, condition = condition, comparison = comparison, change = change, increment = increment)
    }
    return ZAddExtendedCommandCodec.decode(topology.handle(request), cfg.charset)
}
