package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HSET", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface HSetCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg data: FieldValue,
    ): CommandRequest
}
