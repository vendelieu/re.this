package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.SortOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SORT", RedisOperation.WRITE, [RespCode.ARRAY], extensions = [SortOption::class])
fun interface SortCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisKey key: String,
        vararg option: SortOption
    ): CommandRequest<String>
}
