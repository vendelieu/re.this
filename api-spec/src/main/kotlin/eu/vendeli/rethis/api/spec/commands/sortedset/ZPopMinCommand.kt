package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("ZPOPMIN", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface ZPopMinCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        count: Long?,
    ): CommandRequest
}
