package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("RENAME", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface RenameCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        key: String,
        newkey: String
    ): CommandRequest
}
