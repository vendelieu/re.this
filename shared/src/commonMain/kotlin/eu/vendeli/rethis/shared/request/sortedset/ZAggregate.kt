package eu.vendeli.rethis.shared.request.sortedset

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("AGGREGATE")
enum class ZAggregate {
    SUM,
    MIN,
    MAX,
}
