package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LINDEX", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface LIndexCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        index: Long
    ): CommandRequest
}
