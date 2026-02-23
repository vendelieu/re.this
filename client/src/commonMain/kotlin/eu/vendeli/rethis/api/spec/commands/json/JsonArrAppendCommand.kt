package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("JSON.ARRAPPEND", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface JsonArrAppendCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        path: String?,
        vararg value: String
    ): CommandRequest
}
