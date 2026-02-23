package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("RPOP", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface RPopCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        count: Long?
    ): CommandRequest
}
