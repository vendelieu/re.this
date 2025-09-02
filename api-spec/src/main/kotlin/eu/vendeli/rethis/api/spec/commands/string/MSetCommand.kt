package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface MSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        vararg data: KeyValue,
    ): CommandRequest
}
