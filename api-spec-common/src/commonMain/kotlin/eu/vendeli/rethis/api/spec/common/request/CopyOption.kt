package eu.vendeli.rethis.api.spec.common.request

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class CopyOption {
    @RedisOption
    class DB(
        val destinationDb: Long,
    ) : CopyOption()

    @RedisOption
    data object REPLACE : CopyOption()
}
