package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption

sealed class CopyOption {
    @RedisOption
    class DB(
        @RedisOption.Name("destination-db") destination: Long,
    ) : CopyOption()

    @RedisOption
    data object REPLACE : CopyOption()
}
