package eu.vendeli.rethis.api.spec.common.request.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption
data class SlotRange(val startSlot: Long, val endSlot: Long)
