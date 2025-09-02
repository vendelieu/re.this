package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SUNION", RedisOperation.READ, [RespCode.ARRAY, RespCode.SET])
fun interface SUnionCommand : RedisCommandSpec<Set<String>> {
    suspend fun encode(vararg key: String): CommandRequest
}
