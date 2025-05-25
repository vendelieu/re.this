package eu.vendeli.rethis.api.spec.common.request.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisKey

class KeyValue(
    @RedisKey val key: String,
    val value: String
)
