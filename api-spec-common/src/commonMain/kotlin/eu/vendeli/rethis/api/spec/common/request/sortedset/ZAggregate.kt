package eu.vendeli.rethis.api.spec.common.request.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.Token("AGGREGATE")
enum class ZAggregate {
    SUM,
    MIN,
    MAX,
}
