package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.FunctionRestoreOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("FUNCTION RESTORE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [FunctionRestoreOption::class])
fun interface FunctionRestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        serializedValue: ByteArray,
        @RedisOptional option: FunctionRestoreOption?
    ): CommandRequest<Nothing>
}
