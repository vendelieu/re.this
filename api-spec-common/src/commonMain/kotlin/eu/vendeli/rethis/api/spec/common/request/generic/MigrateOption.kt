package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class MigrateOption {

    sealed class Strategy : MigrateOption()

    data object COPY : Strategy()

    data object REPLACE : Strategy()


    sealed class Authorization : MigrateOption()

    @RedisOption.Token("AUTH")
    class Auth(
        val auth: String,
    ) : Authorization()

    @RedisOption.Token("AUTH2")
    class Auth2(
        val username: String,
        val password: String,
    ) : Authorization()

    @RedisOption.Token("KEYS")
    class Keys(
        vararg val keys: String,
    ) : MigrateOption()
}
