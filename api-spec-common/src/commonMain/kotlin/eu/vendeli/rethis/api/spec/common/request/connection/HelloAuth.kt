package eu.vendeli.rethis.api.spec.common.request.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

@RedisOption.Token("AUTH")
class HelloAuth(
    val username: String,
    val password: CharArray,
)
