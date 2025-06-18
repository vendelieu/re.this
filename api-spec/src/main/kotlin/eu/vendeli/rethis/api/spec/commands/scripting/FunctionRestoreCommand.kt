package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.request.scripting.FunctionRestoreOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "FUNCTION RESTORE",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
)
fun interface FunctionRestoreCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisMeta.IgnoreCheck([ValidityCheck.TYPE]) serializedValue: ByteArray,
        policy: FunctionRestoreOption?,
    ): CommandRequest
}
