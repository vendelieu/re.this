package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TYPE", RedisOperation.READ, [RespCode.SIMPLE_STRING])
fun interface TypeCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String): CommandRequest
}
