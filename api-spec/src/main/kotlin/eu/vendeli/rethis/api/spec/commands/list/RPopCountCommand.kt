package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("RPOP", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface RPopCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        count: Long?
    ): CommandRequest
}
