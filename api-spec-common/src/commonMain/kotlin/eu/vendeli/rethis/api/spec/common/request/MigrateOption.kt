package eu.vendeli.rethis.api.spec.common.request

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

    @RedisOption
    class AUTH(
        password: String,
    ) : Authorization()

    @RedisOption.Name("AUTH")
    class AUTH2(
        username: String,
        password: String,
    ) : Authorization()

    @RedisOption
    class KEYS(
        vararg key: String,
    ) : MigrateOption()
}
