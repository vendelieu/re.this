package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.SkipName
data class SlotRange(val start: Int, val end: Int)
