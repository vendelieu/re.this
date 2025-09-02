package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SPOP", RedisOperation.WRITE, [RespCode.BULK, RespCode.NULL])
fun interface SPopCommand : RedisCommandSpec<String> {
    suspend fun encode(key: String): CommandRequest
}
