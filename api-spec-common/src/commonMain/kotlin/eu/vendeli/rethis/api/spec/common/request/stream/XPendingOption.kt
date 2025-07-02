package eu.vendeli.rethis.api.spec.common.request.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

class XPendingMainFilter(
    val start: String,
    val end: String,
    val count: Long,
    @RedisOption.Token("IDLE") val minIdleTime: Long? = null,
    val consumer: String? = null,
)
