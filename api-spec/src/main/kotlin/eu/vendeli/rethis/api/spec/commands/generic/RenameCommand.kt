package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("RENAME", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface RenameCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisKey newkey: String
    ): CommandRequest<List<String>>
}
