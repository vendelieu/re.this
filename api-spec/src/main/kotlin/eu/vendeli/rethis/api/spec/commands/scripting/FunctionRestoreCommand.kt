package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.scripting.FunctionRestoreOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "FUNCTION RESTORE",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
)
fun interface FunctionRestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        serializedValue: ByteArray,
        policy: FunctionRestoreOption?,
    ): CommandRequest
}
