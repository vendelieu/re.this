package eu.vendeli.rethis.shared.request.generic

import eu.vendeli.rethis.shared.annotations.RedisOption


sealed class CopyOption {
    @RedisOption.Token("DB")
    class DB(
        val destinationDb: Long,
    ) : CopyOption()

    data object REPLACE : CopyOption()
}
