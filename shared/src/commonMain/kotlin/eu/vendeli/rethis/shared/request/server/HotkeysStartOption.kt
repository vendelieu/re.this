package eu.vendeli.rethis.shared.request.server

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class HotkeysStartOption {
    @RedisOption.Token("COUNT")
    class Count(@RedisOption.Name("k") val k: Long) : HotkeysStartOption()

    @RedisOption.Token("DURATION")
    class Duration(@RedisOption.Name("seconds") val seconds: Long) : HotkeysStartOption()

    @RedisOption.Token("SAMPLE")
    class Sample(@RedisOption.Name("ratio") val ratio: Long) : HotkeysStartOption()
}
