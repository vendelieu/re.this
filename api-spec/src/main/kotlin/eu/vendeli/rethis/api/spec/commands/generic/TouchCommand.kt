package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("TOUCH", RedisOperation.READ, [RespCode.INTEGER])
fun interface TouchCommand : RedisCommandSpec<Long> {
    suspend fun encode(vararg key: String): CommandRequest
}
