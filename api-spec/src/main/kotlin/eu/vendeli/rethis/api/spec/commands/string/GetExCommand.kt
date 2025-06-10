package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.string.GetExOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "GETEX", 
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
    extensions = [GetExOption::class]
)
fun interface GetExCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOptional vararg option: GetExOption
    ): CommandRequest
}
