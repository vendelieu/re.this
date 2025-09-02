package eu.vendeli.rethis.shared.request.cluster

import eu.vendeli.rethis.shared.annotations.RedisOption

class SlotRange(@RedisOption.Name("startSlot") val start: Long, @RedisOption.Name("endSlot") val end: Long)
