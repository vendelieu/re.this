package eu.vendeli.rethis.shared.request.sortedset

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("LIMIT")
class ZRangeStoreLimit(
    val offset: Long,
    val count: Long,
)
