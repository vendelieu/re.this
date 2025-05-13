package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption
class XPendingOption(
    @RedisOption.Name("MINIDLETIME")
    val minIdleTime: Long? = null,
    val start: String,
    val end: String,
    val count: Long,
    val consumer: String? = null,
)

