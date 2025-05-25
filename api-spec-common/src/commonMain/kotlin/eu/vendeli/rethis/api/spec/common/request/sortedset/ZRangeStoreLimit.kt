package eu.vendeli.rethis.api.spec.common.request.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.Token("LIMIT")
class ZRangeStoreLimit(
    val offset: Long,
    val count: Long,
)
