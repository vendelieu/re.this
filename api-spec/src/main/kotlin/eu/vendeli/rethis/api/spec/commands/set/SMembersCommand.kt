package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SMEMBERS", RedisOperation.READ, [RespCode.ARRAY, RespCode.SET])
fun interface SMembersCommand : RedisCommandSpec<Set<String>> {
    suspend fun encode(key: String): CommandRequest
}
