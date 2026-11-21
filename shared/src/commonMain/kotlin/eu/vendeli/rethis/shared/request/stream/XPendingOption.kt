package eu.vendeli.rethis.shared.request.stream

import eu.vendeli.rethis.shared.annotations.RedisOption

/** @property minIdleTime minimum idle time of a pending entry, in milliseconds. */
class XPendingMainFilter(
    val start: String,
    val end: String,
    val count: Long,
    @RedisOption.Token("IDLE") val minIdleTime: Long? = null,
    val consumer: String? = null,
)
