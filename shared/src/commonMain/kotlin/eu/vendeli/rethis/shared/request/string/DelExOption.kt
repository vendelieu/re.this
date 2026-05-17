package eu.vendeli.rethis.shared.request.string

import eu.vendeli.rethis.shared.annotations.RedisOption

sealed class DelExOption {
    @RedisOption.Token("IFEQ")
    class IfEq(@RedisOption.Name("ifeq-value") val value: String) : DelExOption()

    @RedisOption.Token("IFNE")
    class IfNe(@RedisOption.Name("ifne-value") val value: String) : DelExOption()

    @RedisOption.Token("IFDEQ")
    class IfDigestEq(@RedisOption.Name("ifdeq-digest") val digest: Long) : DelExOption()

    @RedisOption.Token("IFDNE")
    class IfDigestNe(@RedisOption.Name("ifdne-digest") val digest: Long) : DelExOption()
}
