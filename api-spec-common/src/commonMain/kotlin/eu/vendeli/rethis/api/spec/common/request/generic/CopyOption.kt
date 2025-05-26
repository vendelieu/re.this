package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptionContainer

@RedisOptionContainer
sealed class CopyOption {
    @RedisOption.Token("DB")
    class DB(
        val destinationDb: Long,
    ) : CopyOption()

    @RedisOption
    data object REPLACE : CopyOption()
}
