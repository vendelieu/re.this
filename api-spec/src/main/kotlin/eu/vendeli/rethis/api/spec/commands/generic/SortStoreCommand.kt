package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.SortOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "SORT",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
    extensions = [SortOption.Store::class, SortOption::class],
)
fun interface SortStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisMeta.IgnoreCheck([ValidityCheck.OPTIONALITY]) store: SortOption.Store,
        @RedisOptional vararg option: SortOption,
    ): CommandRequest<List<String>>
}
