package eu.vendeli.rethis.shared.request.stream

import eu.vendeli.rethis.shared.annotations.RedisOption

class XPendingMainFilter(
    val start: String,
    val end: String,
    val count: Long,
    @RedisOption.Token("IDLE") val minIdleTime: Long? = null,
    val consumer: String? = null,
)
