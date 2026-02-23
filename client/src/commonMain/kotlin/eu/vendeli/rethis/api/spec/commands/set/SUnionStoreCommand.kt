package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SUNIONSTORE", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SUnionStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        destination: String,
        vararg key: String,
    ): CommandRequest
}
