package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SORT_RO", RedisOperation.READ, [RespCode.ARRAY])
fun interface SortRoCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        @RIgnoreSpecAbsence vararg option: SortOption,
    ): CommandRequest
}
