package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("RPUSHX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface RPushxCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        vararg element: String
    ): CommandRequest<String>
}
