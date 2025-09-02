package eu.vendeli.rethis.shared.request.connection

import eu.vendeli.rethis.shared.annotations.RedisOption

@RedisOption.Token("AUTH")
class HelloAuth(
    val username: String,
    val password: CharArray,
)
