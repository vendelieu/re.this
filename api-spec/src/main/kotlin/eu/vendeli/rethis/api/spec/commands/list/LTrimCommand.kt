package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LTRIM", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface LTrimCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        start: Long,
        stop: Long
    ): CommandRequest
}
