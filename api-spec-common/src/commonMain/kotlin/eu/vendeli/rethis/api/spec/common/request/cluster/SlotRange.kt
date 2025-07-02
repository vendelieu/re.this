package eu.vendeli.rethis.api.spec.common.request.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

class SlotRange(@RedisOption.Name("startSlot") val start: Long, @RedisOption.Name("endSlot") val end: Long)
