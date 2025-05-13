package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.SortOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SORT", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [SortOption.STORE::class])
fun interface SortStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        store: SortOption.STORE,
        vararg option: SortOption
    ): CommandRequest<String>
}
