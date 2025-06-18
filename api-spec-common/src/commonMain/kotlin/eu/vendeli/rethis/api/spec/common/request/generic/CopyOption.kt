package eu.vendeli.rethis.api.spec.common.request.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisOption


sealed class CopyOption {
    @RedisOption.Token("DB")
    class DB(
        val destinationDb: Long,
    ) : CopyOption()

    data object REPLACE : CopyOption()
}
