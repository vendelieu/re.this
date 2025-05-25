package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

sealed class MigrateOption {
    @RedisOptionContainer
    sealed class Strategy : MigrateOption()

    @RedisOption
    data object COPY : Strategy()

    @RedisOption
    data object REPLACE : Strategy()

    @RedisOptionContainer
    sealed class Authorization : MigrateOption()

    @RedisOption.Token("AUTH")
    class Auth(
        val password: String,
    ) : Authorization()

    @RedisOption.Token("AUTH2")
    class Auth2(
        val username: String,
        val password: String,
    ) : Authorization()

    @RedisOption.Token("KEYS")
    class Keys(
        @RedisKey vararg val key: String,
    ) : MigrateOption()
}
