package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption
class JsonEntry(
    val key: String,
    val path: String,
    val value: String,
)
