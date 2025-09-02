package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HGET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface HGetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        field: String
    ): CommandRequest
}
