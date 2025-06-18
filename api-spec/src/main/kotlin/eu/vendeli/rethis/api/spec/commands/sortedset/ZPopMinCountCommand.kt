package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.response.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZPOPMIN", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface ZPopMinCountCommand : RedisCommandSpec<List<List<ZMember>>> {
    suspend fun encode(
        key: String,
        count: Long?,
    ): CommandRequest
}
