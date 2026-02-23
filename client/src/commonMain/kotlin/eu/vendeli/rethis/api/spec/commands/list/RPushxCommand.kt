package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("RPUSHX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface RPushxCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg element: String
    ): CommandRequest
}
