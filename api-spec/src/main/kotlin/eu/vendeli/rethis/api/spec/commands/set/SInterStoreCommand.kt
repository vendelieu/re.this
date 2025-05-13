package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SINTERSTORE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SInterStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey destination: String,
        @RedisKey vararg key: String,
    ): CommandRequest<List<String>>
}
