package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SPOP", RedisOperation.WRITE, [RespCode.BULK, RespCode.NULL])
fun interface SPopCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String): CommandRequest
}
