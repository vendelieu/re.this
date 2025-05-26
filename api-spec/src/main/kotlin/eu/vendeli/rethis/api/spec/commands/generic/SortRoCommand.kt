package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.generic.SortRoOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SORT_RO", RedisOperation.READ, [RespCode.ARRAY], extensions = [SortRoOption::class])
fun interface SortRoCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional vararg option: SortRoOption
    ): CommandRequest<List<String>>
}
